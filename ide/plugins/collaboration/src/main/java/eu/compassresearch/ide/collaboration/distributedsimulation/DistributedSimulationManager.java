package eu.compassresearch.ide.collaboration.distributedsimulation;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.jface.window.Window;

import eu.compassresearch.ide.collaboration.Activator;
import eu.compassresearch.ide.collaboration.communication.ConnectionManager;
import eu.compassresearch.ide.collaboration.communication.messages.SimulationReplyMessage;
import eu.compassresearch.ide.collaboration.communication.messages.SimulationRequestMessage;
import eu.compassresearch.ide.collaboration.communication.messages.SimulationStartMessage;
import eu.compassresearch.ide.collaboration.datamodel.CollaborationGroup;
import eu.compassresearch.ide.collaboration.datamodel.CollaborationProject;
import eu.compassresearch.ide.collaboration.datamodel.User;
import eu.compassresearch.ide.collaboration.notifications.Notification;
import eu.compassresearch.ide.collaboration.ui.menu.CollaborationDialogs;
import eu.compassresearch.ide.collaboration.ui.menu.DistributedSimulationInitialisationDialog;
import eu.compassresearch.ide.collaboration.ui.menu.DistributedSimulationRequestDialog;
import eu.compassresearch.ide.core.resources.ICmlModel;
import eu.compassresearch.ide.core.resources.ICmlProject;
import eu.compassresearch.ide.interpreter.CmlUtil;
import eu.compassresearch.ide.interpreter.ICmlDebugConstants;

public class DistributedSimulationManager
{
	//collaborator to external process mapping
	private Map<String, List<DistributedSimulationConfiguration>> configurations;
	//to update GUI
	private Map<String, List<IDistributedSimulationListener>> listeners;
	private ConnectionManager connectionManager;
	private CollaborationProject projectForCurrentSession;
	private DistributedSimulationInitialisationDialog distSimDlg;
	private boolean do_animation;
	private String serverHostAddress;
	private String selectedTopProcess;

	public DistributedSimulationManager(ConnectionManager connectionManager)
	{
		this.connectionManager = connectionManager;
		configurations = new HashMap<String, List<DistributedSimulationConfiguration>>();
		listeners = new HashMap<String, List<IDistributedSimulationListener>>();
		
		do_animation = false;
	}

	//Server Side 
	public void addConfiguration(
			DistributedSimulationConfiguration distributedConfiguration,
			IDistributedSimulationListener notify)
	{
		String collaboratorName = distributedConfiguration.getCollaborator();

		if (!configurations.containsKey(collaboratorName))
		{
			configurations.put(collaboratorName, new ArrayList<DistributedSimulationConfiguration>());
		}
		configurations.get(collaboratorName).add(distributedConfiguration);

		String collaborator = distributedConfiguration.getCollaborator();
		addListener(collaborator, notify);
	}

	//Server Side
	public void initiateCollaborationOnProject(CollaborationProject project)
	{
		newSimulationSession();

		projectForCurrentSession = project;
		String projectWorkspaceName = project.getProjectWorkspaceName();
		List<User> joinedCollaborators = project.getCollaboratorGroup().getJoinedCollaborators();

		// load collaborators
		ArrayList<String> collaborators = new ArrayList<>();
		for (User user : joinedCollaborators)
		{
			collaborators.add(user.getName());
		}

		// only show online collaborators for the distributed simulation
		ConnectionManager connectionManager = Activator.getDefault().getConnectionManager();
		collaborators = connectionManager.retainOnlineCollaborators(collaborators);

		// load processes
		IProject iProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectWorkspaceName);
		if (iProject == null)
		{
			return;
		}
		ICmlProject cmlProj = (ICmlProject) iProject.getAdapter(ICmlProject.class);
		ICmlModel model = cmlProj.getModel();
		List<String> processes = CmlUtil.getGlobalProcessesFromSourceAsString(model);

		if (processes.isEmpty())
		{
			CollaborationDialogs.getInstance().displayNotificationPopup("Distributed Simulation", Notification.Dist_Simulation_ERROR_NO_PROCESSES);
			return;
		}

		if (collaborators.isEmpty())
		{
			CollaborationDialogs.getInstance().displayNotificationPopup("Distributed Simulation", Notification.Dist_Simulation_ERROR_NO_COLLABORATORS);
			return;
		}

		distSimDlg = CollaborationDialogs.getInstance().getDistributedSimulationInitialisationDialog(processes, collaborators, this);

		if (distSimDlg.open() == Window.OK)
		{
			startDistributedSimulation(project);
		}
	}

	//Server Side
	public void startDistributedSimulation(
			CollaborationProject collaborationProject)
	{
		CollaborationGroup collaboratorGroup = collaborationProject.getCollaboratorGroup();
		ID sender = connectionManager.getConnectedUser();
		String uniqueID = collaborationProject.getUniqueID();
		String hostAddress = getServerHostAddress();
		
		// notify collaborators that simulation is starting
		for (Entry<String, List<DistributedSimulationConfiguration>> configEntry : configurations.entrySet())
		{
			User user = collaboratorGroup.getUser(configEntry.getKey());
			if (user != null)
			{
				for (DistributedSimulationConfiguration config : configEntry.getValue())
				{
					if (config.isApproved())
					{
						SimulationStartMessage simStartMsg = new SimulationStartMessage(sender, uniqueID, config.getProcess());
						connectionManager.sendTo(user, simStartMsg);
					}
				}
			}
		}

		// build external process list from configurations
		ArrayList<DistributedSimulationConfiguration> configs = getConfigurations();
		StringBuffer externalProcessStrBld = new StringBuffer();
		DistributedSimulationConfiguration config;
		for (Iterator<DistributedSimulationConfiguration> iterator = configs.iterator(); iterator.hasNext();)
		{
			config = iterator.next();
			externalProcessStrBld.append(config.getProcess());
			if (iterator.hasNext())
			{
				externalProcessStrBld.append(",");
			}
		}

		launchSimulatorServerSide(collaborationProject, selectedTopProcess, externalProcessStrBld.toString(), hostAddress);
	}

	//Server Side
	private void launchSimulatorServerSide(
			CollaborationProject collaborationProject, String topProcess,
			String externalProceses, String hostAddress)
	{
		IProject eclipseProject = ResourcesPlugin.getWorkspace().getRoot().getProject(collaborationProject.getProjectWorkspaceName());

		try
		{
			ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType ctype = launchManager.getLaunchConfigurationType("eu.compassresearch.ide.interpreter.launchConfigurationTypeCoSim");

			ILaunchConfigurationWorkingCopy lcwc = ctype.newInstance(null, launchManager.generateLaunchConfigurationName(collaborationProject.getName()));

			// eclipse project
			lcwc.setAttribute(ICmlDebugConstants.CML_LAUNCH_CONFIG_PROJECT, eclipseProject.getName());

			// launch mode
			lcwc.setAttribute(ICmlDebugConstants.CML_LAUNCH_CONFIG_IS_ANIMATION, do_animation);
			// is coordinator
			lcwc.setAttribute(ICmlDebugConstants.CML_LAUNCH_CONFIG_COSIM_IS_COORDINATOR, true);

			// top process
			lcwc.setAttribute(ICmlDebugConstants.CML_LAUNCH_CONFIG_PROCESS_NAME, topProcess);
			// set processes
			lcwc.setAttribute(ICmlDebugConstants.CML_LAUNCH_CONFIG_COSIM_EXTERNAL_PROCESSES, externalProceses);

			// set host
			lcwc.setAttribute(ICmlDebugConstants.CML_LAUNCH_CONFIG_COSIM_HOST,  /*hostAddress*/  "localhost:8882");

			// start debugger
			DebugUITools.launch(lcwc, "debug");

		} catch (CoreException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Server Side
	public void simulationRequestUpdate(String collaborator, String projectID,
			String process, boolean accepted)
	{
		if (accepted)
		{
			// approve configuration for collaborator
			List<DistributedSimulationConfiguration> collaboratorConfigs = configurations.get(collaborator);
			for (DistributedSimulationConfiguration config : collaboratorConfigs)
			{
				config.approve(process, collaborator);
			}
		}

		// update UI
		if (listeners.containsKey(collaborator))
		{
			List<IDistributedSimulationListener> notifyList = listeners.get(collaborator);

			for (IDistributedSimulationListener notify : notifyList)
			{
				notify.onRequestUpdated(process, accepted);
			}
		}

		// has all configurations between approved?
		boolean readyForSimulation = true;
		ArrayList<DistributedSimulationConfiguration> configs = getConfigurations();
		for (DistributedSimulationConfiguration config : configs)
		{
			if (!config.isApproved())
			{
				readyForSimulation = false;
				break;
			}
		}

		if (readyForSimulation)
		{
			distSimDlg.readyForSimulation();
		}
	}

	//Server Side
	private ArrayList<DistributedSimulationConfiguration> getConfigurations()
	{
		ArrayList<DistributedSimulationConfiguration> configList = new ArrayList<DistributedSimulationConfiguration>();
		for (List<DistributedSimulationConfiguration> configs : configurations.values())
		{
			configList.addAll(configs);
		}

		return configList;
	}
	
	//Server Side
	public void setTopProcess(String selectedTopProcess)
	{
		this.selectedTopProcess = selectedTopProcess;
	}
	
	//Server Side
	public void setLaunchMode(boolean animation)
	{
		do_animation = animation;
	}

	//Server side
	public void requestSimulationFromCollaborators()
	{
		CollaborationGroup collaboratorGroup = projectForCurrentSession.getCollaboratorGroup();
		String uniqueID = projectForCurrentSession.getUniqueID();
		ID sender = connectionManager.getConnectedUser();
		String serverHostAddress = getServerHostAddress();
		
		for (Entry<String, List<DistributedSimulationConfiguration>> configEntry : configurations.entrySet())
		{
			User user = collaboratorGroup.getUser(configEntry.getKey());
			if (user != null)
			{
				for (DistributedSimulationConfiguration config : configEntry.getValue())
				{
					SimulationRequestMessage simRequest = new SimulationRequestMessage(sender, uniqueID, config.getProcess(), serverHostAddress);
					connectionManager.sendTo(user, simRequest);
				}
			}
		}
	}

	//Client Side
	public void newSimulationRequest(String sender,
			CollaborationProject collaborationProject, String process, String remoteHost)
	{
		newSimulationSession();

		DistributedSimulationRequestDialog distributedSimulationRequestDialog = 
				CollaborationDialogs.getInstance().getDistributedSimulationRequestDialog(sender, collaborationProject, process, this);

		if (distributedSimulationRequestDialog.open() == Window.OK)
		{
			launchClientSide(collaborationProject, process, remoteHost);
		}
	}

	//Client Side
	public void approveSimulation(String requestedByUser,
			CollaborationProject collaborationProject, String process,
			IDistributedSimulationListener notifyOnStart)
	{
		User requestedByuserObj = collaborationProject.getCollaboratorGroup().getUser(requestedByUser);
		final ID sender = connectionManager.getConnectedUser();

		SimulationReplyMessage replyMsg = new SimulationReplyMessage(sender, collaborationProject.getUniqueID(), process, true);
		connectionManager.sendTo(requestedByuserObj, replyMsg);

		addListener(requestedByuserObj.getName(), notifyOnStart);
	}

	//Client Side
	public void declineSimulation(String user,
			CollaborationProject collaborationProject, String process)
	{
		User userObj = collaborationProject.getCollaboratorGroup().getUser(user);
		final ID sender = connectionManager.getConnectedUser();

		SimulationReplyMessage replyMsg = new SimulationReplyMessage(sender, collaborationProject.getUniqueID(), process, false);

		connectionManager.sendTo(userObj, replyMsg);
	}

	//Client Side
	public void simulationStarting(String collaborator, String projectID,
			String process)
	{
		// update UI
		if (listeners.containsKey(collaborator))
		{
			List<IDistributedSimulationListener> notifyList = listeners.get(collaborator);

			for (IDistributedSimulationListener notify : notifyList)
			{
				notify.onSimluationStarting();
			}
		}
	}
	
	//Client Side
	private void launchClientSide(CollaborationProject collaborationProject,
			String topProcess, String remoteHost)
	{
		IProject eclipseProject = ResourcesPlugin.getWorkspace().getRoot().getProject(collaborationProject.getProjectWorkspaceName());

		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType ctype = launchManager.getLaunchConfigurationType("eu.compassresearch.ide.interpreter.launchConfigurationTypeCoSim");

		ILaunchConfigurationWorkingCopy lcwc;
		try
		{
			lcwc = ctype.newInstance(null, launchManager.generateLaunchConfigurationName(collaborationProject.getName()));

			// eclipse project
			lcwc.setAttribute(ICmlDebugConstants.CML_LAUNCH_CONFIG_PROJECT, eclipseProject.getName());

			// launch mode
			lcwc.setAttribute(ICmlDebugConstants.CML_LAUNCH_CONFIG_IS_ANIMATION, true);
			// is coordinator
			lcwc.setAttribute(ICmlDebugConstants.CML_LAUNCH_CONFIG_COSIM_IS_COORDINATOR, false);

			// TODO get from GUI
			// top process
			lcwc.setAttribute(ICmlDebugConstants.CML_LAUNCH_CONFIG_PROCESS_NAME, topProcess);
			
			// set host
			lcwc.setAttribute(ICmlDebugConstants.CML_LAUNCH_CONFIG_COSIM_HOST,  /*remoteHost*/ "localhost:8882" );

			// start debugger
			DebugUITools.launch(lcwc, "debug");

		} catch (CoreException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Client Side
	public void addListener(String collaborator,
			IDistributedSimulationListener notify)
	{
		if (!listeners.containsKey(collaborator))
		{
			listeners.put(collaborator, new ArrayList<IDistributedSimulationListener>());
		}
		
		listeners.get(collaborator).add(notify);
	}

	private void newSimulationSession()
	{
		listeners.clear();
		configurations.clear();
		distSimDlg = null;
		serverHostAddress = null;
		selectedTopProcess = null;
	}
	
	private String getServerHostAddress()
	{
		try
		{
			serverHostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return serverHostAddress;
	}
}

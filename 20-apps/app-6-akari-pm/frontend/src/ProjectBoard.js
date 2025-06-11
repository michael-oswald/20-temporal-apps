import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { AppBar, Toolbar, Typography, Container, Box, Card, CardContent, Button, Grid, Stack, Paper, Divider } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import AssignmentIndIcon from '@mui/icons-material/AssignmentInd';
import PlaylistAddIcon from '@mui/icons-material/PlaylistAdd';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import TextField from '@mui/material/TextField';
import Avatar from '@mui/material/Avatar';
import PersonIcon from '@mui/icons-material/Person';
import MenuItem from '@mui/material/MenuItem';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import DragIndicatorIcon from '@mui/icons-material/DragIndicator';
import DeleteIcon from '@mui/icons-material/Delete';
import { DndContext, closestCenter } from '@dnd-kit/core';
import { SortableContext, horizontalListSortingStrategy, useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import './App.css';

function ProjectBoard({ projectId, projectName, onBack }) {
  const API_BASE = `http://localhost:8081/project/${projectId}`;

  const [project, setProject] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [addTaskOpen, setAddTaskOpen] = useState(false);
  const [newTask, setNewTask] = useState({ name: '', trackId: '', description: '', dayEstimate: '' });
  const [addTaskError, setAddTaskError] = useState('');
  const [addTrackOpen, setAddTrackOpen] = useState(false);
  const [newTrack, setNewTrack] = useState({ trackName: '' });
  const [addTrackError, setAddTrackError] = useState('');
  const [addWorkerOpen, setAddWorkerOpen] = useState(false);
  const [newWorker, setNewWorker] = useState({ name: '' });
  const [addWorkerError, setAddWorkerError] = useState('');
  const [assignDialogOpen, setAssignDialogOpen] = useState(false);
  const [assignTaskId, setAssignTaskId] = useState('');
  const [assignWorkerId, setAssignWorkerId] = useState('');
  const [assignError, setAssignError] = useState('');
  const [editTaskOpen, setEditTaskOpen] = useState(false);
  const [editTask, setEditTask] = useState(null);
  const [editTaskError, setEditTaskError] = useState('');
  const [editTrackOpen, setEditTrackOpen] = useState(false);
  const [editTrack, setEditTrack] = useState(null);
  const [editTrackError, setEditTrackError] = useState('');
  const [moveTaskDialogOpen, setMoveTaskDialogOpen] = useState(false);
  const [moveTaskId, setMoveTaskId] = useState('');
  const [moveTaskCurrentTrackId, setMoveTaskCurrentTrackId] = useState('');
  const [moveTaskNewTrackId, setMoveTaskNewTrackId] = useState('');
  const [moveTaskError, setMoveTaskError] = useState('');

  const getStatusColor = (status) => {
    switch (status) {
      case 'IN_PROGRESS':
        return 'info.main'; // blue
      case 'COMPLETE':
        return 'success.main'; // green
      case 'BLOCKED':
        return 'error.main'; // red
      case 'NOT_STARTED':
      case '':
      case undefined:
      default:
        return 'grey.400'; // gray
    }
  };

  function getStatusClass(status) {
    switch (status) {
      case 'NOT_STARTED':
        return 'not_started';
      case 'IN_PROGRESS':
        return 'in_progress';
      case 'COMPLETE':
        return 'complete';
      case 'BLOCKED':
        return 'blocked';
      default:
        return '';
    }
  }

  useEffect(() => {
    axios.get(`${API_BASE}/details`)
      .then(res => {
        setProject(res.data);
        setLoading(false);
      })
      .catch(err => {
        setError('Failed to fetch project details');
        setLoading(false);
      });
  }, [API_BASE]);

  if (loading) return <Typography>Loading...</Typography>;
  if (error) return <Typography color="error">{error}</Typography>;
  if (!project) return null;

  // Group tracks and tasks based on backend structure
  const tracks = project.tracks || [];

  // Separate backlog and other tracks using trackName and trackId
  const backlogTrack = tracks.find(t => t.trackName && t.trackName.toLowerCase() === 'backlog');
  const otherTracks = tracks.filter(t => t.trackName && t.trackName.toLowerCase() !== 'backlog');

  // Find first non-backlog track for default
  const firstTrackId = (otherTracks[0] && otherTracks[0].trackId) || '';

  const handleAddTaskOpen = () => {
    setNewTask({ name: '', trackId: firstTrackId, description: '', dayEstimate: '' });
    setAddTaskError('');
    setAddTaskOpen(true);
  };
  const handleAddTaskClose = () => setAddTaskOpen(false);

  const handleAddTaskChange = (e) => {
    setNewTask({ ...newTask, [e.target.name]: e.target.value });
  };

  const handleAddTaskSubmit = async () => {
    if (!newTask.name || !newTask.trackId || newTask.dayEstimate === '' || isNaN(Number(newTask.dayEstimate)) || Number(newTask.dayEstimate) <= 0) {
      setAddTaskError('Task name, track, and a positive day estimate are required.');
      return;
    }
    try {
      const response = await axios.post(`${API_BASE}/tasks`, {
        name: newTask.name,
        trackId: newTask.trackId,
        description: newTask.description,
        dayEstimate: Number(newTask.dayEstimate)
      });

      setAddTaskOpen(false);
      setAddTaskError('');

      // Optimistic UI update - add the task to the local state
      const newTaskData = {
        ...newTask,
        taskId: response.data.taskId || Date.now().toString(), // Use returned ID or temporary ID
        status: 'NOT_STARTED'
      };

      // Find the track and add the task to it
      const updatedTracks = project.tracks.map(track => {
        if (track.trackId === newTask.trackId) {
          return {
            ...track,
            tasks: [...(track.tasks || []), newTaskData]
          };
        }
        return track;
      });

      // Update local state immediately
      setProject({
        ...project,
        tracks: updatedTracks
      });

      // Then refresh from server in the background
      setTimeout(async () => {
        try {
          const res = await axios.get(`${API_BASE}/details`);
          setProject(res.data);
        } catch (err) {
          console.error("Background refresh failed:", err);
        }
      }, 1000); // Wait 1 second before refreshing from server

    } catch (err) {
      setAddTaskError('Failed to add task.');
    }
  };

  const handleAddTrackOpen = () => {
    setNewTrack({ trackName: '' });
    setAddTrackError('');
    setAddTrackOpen(true);
  };
  const handleAddTrackClose = () => setAddTrackOpen(false);

  const handleAddTrackChange = (e) => {
    setNewTrack({ ...newTrack, [e.target.name]: e.target.value });
  };

  const handleAddTrackSubmit = async () => {
    if (!newTrack.trackName) {
      setAddTrackError('Track name is required.');
      return;
    }
    try {
      // Generate a temporary ID for the new track
      const tempTrackId = 'temp-' + Date.now();

      // Create an optimistic track object
      const optimisticTrack = {
        trackId: tempTrackId,
        trackName: newTrack.trackName,
        tasks: []
      };

      // Close dialog
      setAddTrackOpen(false);
      setAddTrackError('');

      // Update UI immediately with optimistic track
      setProject({
        ...project,
        tracks: [...project.tracks, optimisticTrack]
      });

      // Make the API call in the background
      const response = await axios.post(`${API_BASE}/tracks`, {
        trackName: newTrack.trackName
      });

      // Refresh data from server after a short delay
      setTimeout(async () => {
        try {
          const res = await axios.get(`${API_BASE}/details`);
          setProject(res.data);
        } catch (err) {
          console.error("Background refresh failed:", err);
          // If API call fails, remove the optimistic track
          setProject({
            ...project,
            tracks: project.tracks.filter(track => track.trackId !== tempTrackId)
          });
          alert('Failed to add track. Changes reverted.');
        }
      }, 1000);
    } catch (err) {
      setAddTrackError('Failed to add track.');
    }
  };

  const handleAddWorkerOpen = () => {
    setNewWorker({ name: '' });
    setAddWorkerError('');
    setAddWorkerOpen(true);
  };
  const handleAddWorkerClose = () => setAddWorkerOpen(false);

  const handleAddWorkerChange = (e) => {
    setNewWorker({ ...newWorker, [e.target.name]: e.target.value });
  };

  const handleAddWorkerSubmit = async () => {
    if (!newWorker.name) {
      setAddWorkerError('Worker name is required.');
      return;
    }
    try {
      // Generate a temporary ID for optimistic update
      const tempWorkerId = 'temp-' + Date.now();

      // Create an optimistic worker object
      const optimisticWorker = {
        userId: tempWorkerId,
        name: newWorker.name
      };

      // Close dialog
      setAddWorkerOpen(false);
      setAddWorkerError('');

      // Update UI immediately with optimistic worker
      setProject({
        ...project,
        workers: [...(project.workers || []), optimisticWorker]
      });

      // Make the API call in the background
      await axios.post(`${API_BASE}/workers`, {
        name: newWorker.name
      });

      // Refresh data from server after a short delay
      setTimeout(async () => {
        try {
          const res = await axios.get(`${API_BASE}/details`);
          setProject(res.data);
        } catch (err) {
          console.error("Background refresh failed:", err);
          // If API call fails, remove the optimistic worker
          setProject({
            ...project,
            workers: (project.workers || []).filter(worker => worker.userId !== tempWorkerId)
          });
          alert('Failed to add team member. Changes reverted.');
        }
      }, 1000);
    } catch (err) {
      setAddWorkerError('Failed to add worker.');
    }
  };

  const handleAssignOpen = (taskId) => {
    setAssignTaskId(taskId);
    setAssignWorkerId('');
    setAssignError('');
    setAssignDialogOpen(true);
  };
  const handleAssignClose = () => setAssignDialogOpen(false);

  const handleAssignWorkerChange = (e) => {
    setAssignWorkerId(e.target.value);
  };

  const handleAssignSubmit = async () => {
    if (!assignWorkerId) {
      setAssignError('Please select a worker.');
      return;
    }
    try {
      // Store the original state for rollback if needed
      const originalTracks = JSON.parse(JSON.stringify(project.tracks));

      // Find the selected worker
      const selectedWorker = project.workers.find(w => w.userId === assignWorkerId);

      // Optimistic UI update - update the task assignment immediately
      const updatedTracks = project.tracks.map(track => {
        const updatedTasks = track.tasks.map(task => {
          if (task.taskId === assignTaskId) {
            return {
              ...task,
              assignedUserId: assignWorkerId
            };
          }
          return task;
        });
        return { ...track, tasks: updatedTasks };
      });

      // Update UI immediately
      setProject({
        ...project,
        tracks: updatedTracks
      });

      // Close dialog
      setAssignDialogOpen(false);
      setAssignError('');

      // Make API call in background
      await axios.post(`${API_BASE}/assign?workerId=${assignWorkerId}&taskId=${assignTaskId}`);

      // Refresh from server after delay to ensure consistency
      setTimeout(async () => {
        try {
          const res = await axios.get(`${API_BASE}/details`);
          setProject(res.data);
        } catch (err) {
          console.error("Background refresh failed:", err);
          // Rollback to original state if API call fails
          setProject({
            ...project,
            tracks: originalTracks
          });
          alert('Failed to assign worker. Changes reverted.');
        }
      }, 1000);
    } catch (err) {
      setAssignError('Failed to assign worker.');
    }
  };

  const handleEditOpen = (task) => {
    setEditTask({ ...task });
    setEditTaskError('');
    setEditTaskOpen(true);
  };
  const handleEditClose = () => setEditTaskOpen(false);

  const handleEditChange = (e) => {
    setEditTask({ ...editTask, [e.target.name]: e.target.value });
  };

  const handleEditSubmit = async () => {
    if (!editTask.name) {
      setEditTaskError('Task name is required.');
      return;
    }

    try {
      // Store the original tasks for rollback if needed
      const originalTracks = JSON.parse(JSON.stringify(project.tracks));

      // Optimistic UI update - immediately update the task in the UI
      const updatedTracks = project.tracks.map(track => {
        const updatedTasks = track.tasks.map(task => {
          if (task.taskId === editTask.taskId) {
            return { ...editTask };
          }
          return task;
        });
        return { ...track, tasks: updatedTasks };
      });

      // Update local state immediately
      setProject({
        ...project,
        tracks: updatedTracks
      });

      // Close dialog
      setEditTaskOpen(false);
      setEditTaskError('');

      // Send API request in the background
      await axios.post(`${API_BASE}/tasks/edit`, {
        ...editTask
      });

      // Refresh from server after a delay
      setTimeout(async () => {
        try {
          const res = await axios.get(`${API_BASE}/details`);
          setProject(res.data);
        } catch (err) {
          console.error("Background refresh failed:", err);
          // Rollback to original state if API call fails
          setProject({
            ...project,
            tracks: originalTracks
          });
        }
      }, 1000);
    } catch (err) {
      setEditTaskError('Failed to edit task.');
      setEditTaskOpen(true);
    }
  };

  const handleEditTrackOpen = (track) => {
    setEditTrack({
      trackId: track.trackId,
      trackName: track.trackName || '',
      desiredDueDate: track.desiredDueDate ? track.desiredDueDate.substring(0, 10) : '', // yyyy-mm-dd
    });
    setEditTrackError('');
    setEditTrackOpen(true);
  };
  const handleEditTrackClose = () => setEditTrackOpen(false);

  const handleEditTrackChange = (e) => {
    setEditTrack({ ...editTrack, [e.target.name]: e.target.value });
  };

  const handleEditTrackSubmit = async () => {
    if (!editTrack.trackName) {
      setEditTrackError('Track name is required.');
      return;
    }
    // Convert date to full ISO string if present
    let desiredDueDate = editTrack.desiredDueDate || null;
    if (desiredDueDate && desiredDueDate.length === 10) {
      desiredDueDate = desiredDueDate + 'T00:00:00';
    }
    try {
      // Store original data for rollback if needed
      const originalTracks = JSON.parse(JSON.stringify(project.tracks));

      // Optimistic UI update - update the track name immediately in the UI
      const updatedTracks = project.tracks.map(track => {
        if (track.trackId === editTrack.trackId) {
          return {
            ...track,
            trackName: editTrack.trackName,
            desiredDueDate: desiredDueDate
          };
        }
        return track;
      });

      // Update UI immediately
      setProject({
        ...project,
        tracks: updatedTracks
      });

      // Close dialog
      setEditTrackOpen(false);
      setEditTrackError('');

      // Make the API call in the background
      await axios.post(`${API_BASE}/tracks/edit`, {
        trackId: editTrack.trackId,
        trackName: editTrack.trackName,
        desiredDueDate: desiredDueDate,
      });

      // Refresh data from server after a short delay
      setTimeout(async () => {
        try {
          const res = await axios.get(`${API_BASE}/details`);
          setProject(res.data);
        } catch (err) {
          console.error("Background refresh failed:", err);
          // Rollback to original state if API call fails
          setProject({
            ...project,
            tracks: originalTracks
          });
          setEditTrackError('Failed to edit track. Changes reverted.');
        }
      }, 1000);
    } catch (err) {
      setEditTrackError('Failed to edit track.');
    }
  };

  const handleDeleteTask = async (taskId) => {
    if (!window.confirm('Are you sure you want to delete this task?')) return;
    try {
      // Store original data for rollback if needed
      const originalTracks = JSON.parse(JSON.stringify(project.tracks));

      // Optimistic UI update - immediately remove the task from UI
      const updatedTracks = project.tracks.map(track => {
        return {
          ...track,
          tasks: track.tasks.filter(task => task.taskId !== taskId)
        };
      });

      // Update UI immediately
      setProject({
        ...project,
        tracks: updatedTracks
      });

      // Make the API call in the background
      await axios.post(`${API_BASE}/tasks/delete`, null, { params: { taskId } });

      // Refresh data from server after a short delay
      setTimeout(async () => {
        try {
          const res = await axios.get(`${API_BASE}/details`);
          setProject(res.data);
        } catch (err) {
          console.error("Background refresh failed:", err);
          // Rollback to original state if API call fails
          setProject({
            ...project,
            tracks: originalTracks
          });
          alert('Failed to delete task. Changes reverted.');
        }
      }, 1000);
    } catch (err) {
      alert('Failed to delete task.');
    }
  };

  const handleDeleteTrack = async (trackId) => {
    if (!window.confirm('Are you sure you want to delete this track and all its tasks?')) return;
    try {
      // Store original data for rollback if needed
      const originalTracks = JSON.parse(JSON.stringify(project.tracks));

      // Optimistic UI update - immediately remove the track from UI
      const updatedTracks = project.tracks.filter(track => track.trackId !== trackId);
      setProject({
        ...project,
        tracks: updatedTracks
      });

      // Make the API call in the background
      await axios.post(`${API_BASE}/tracks/delete`, null, { params: { trackId } });

      // Refresh data from server after a short delay
      setTimeout(async () => {
        try {
          const res = await axios.get(`${API_BASE}/details`);
          setProject(res.data);
        } catch (err) {
          console.error("Background refresh failed:", err);
          // Rollback to original state if API call fails
          setProject({
            ...project,
            tracks: originalTracks
          });
          alert('Failed to delete track. Changes reverted.');
        }
      }, 1000);
    } catch (err) {
      alert('Failed to delete track.');
    }
  };

  const handleMoveTaskOpen = (taskId, currentTrackId) => {
    setMoveTaskId(taskId);
    setMoveTaskCurrentTrackId(currentTrackId);
    setMoveTaskNewTrackId('');
    setMoveTaskError('');
    setMoveTaskDialogOpen(true);
  };
  const handleMoveTaskClose = () => setMoveTaskDialogOpen(false);

  const handleMoveTaskChange = (e) => {
    setMoveTaskNewTrackId(e.target.value);
  };

  const handleMoveTaskSubmit = async () => {
    if (!moveTaskNewTrackId) {
      setMoveTaskError('Please select a new track.');
      return;
    }

    try {
      // Store the original state for rollback if needed
      const originalTracks = JSON.parse(JSON.stringify(project.tracks));

      // Find the task to move
      let taskToMove = null;
      let sourceTrackId = moveTaskCurrentTrackId;

      // Find and create a copy of the task to move
      project.tracks.forEach(track => {
        if (track.trackId === sourceTrackId) {
          const foundTask = track.tasks.find(t => t.taskId === moveTaskId);
          if (foundTask) {
            taskToMove = { ...foundTask };
          }
        }
      });

      if (!taskToMove) {
        setMoveTaskError('Task not found.');
        return;
      }

      // Update taskToMove with new trackId
      taskToMove.trackId = moveTaskNewTrackId;

      // Create new track arrays by filtering out the moved task from source
      // and adding it to the destination
      const updatedTracks = project.tracks.map(track => {
        if (track.trackId === sourceTrackId) {
          // Remove task from source track
          return {
            ...track,
            tasks: track.tasks.filter(t => t.taskId !== moveTaskId)
          };
        } else if (track.trackId === moveTaskNewTrackId) {
          // Add task to destination track
          return {
            ...track,
            tasks: [...track.tasks, taskToMove]
          };
        }
        return track;
      });

      // Update UI immediately
      setProject({
        ...project,
        tracks: updatedTracks
      });

      // Close dialog
      setMoveTaskDialogOpen(false);
      setMoveTaskError('');

      // Make API call in background
      await axios.post(`${API_BASE}/tasks/changeTrack`, null, {
        params: { taskId: moveTaskId, newTrackId: moveTaskNewTrackId }
      });

      // Refresh from server after delay to ensure consistency
      setTimeout(async () => {
        try {
          const res = await axios.get(`${API_BASE}/details`);
          setProject(res.data);
        } catch (err) {
          console.error("Background refresh failed:", err);
          // Rollback to original state if API call fails
          setProject({
            ...project,
            tracks: originalTracks
          });
        }
      }, 1000);
    } catch (err) {
      setMoveTaskError('Failed to move task.');
    }
  };

  function SortableTask({ task, index, handleEditOpen, handleAssignOpen, getStatusColor, getStatusClass, assignedWorker, handleMoveTaskOpen, currentTrackId }) {
    const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({ id: task.taskId });
    // Use MUI theme for color resolution
    const theme = require('@mui/material/styles').useTheme ? require('@mui/material/styles').useTheme() : {};
    let resolvedColor = getStatusColor(task.status);
    if (theme && theme.palette && typeof resolvedColor === 'string' && resolvedColor.includes('.')) {
      const [color, shade] = resolvedColor.split('.');
      resolvedColor = theme.palette[color] && theme.palette[color][shade] ? resolvedColor : resolvedColor;
    }
    const style = {
      transform: CSS.Transform.toString(transform),
      transition,
      opacity: isDragging ? 0.5 : 1,
      minWidth: 220,
      maxWidth: 260,
      marginRight: 16,
      background: '#fff',
      borderRadius: 8,
      boxShadow: isDragging ? '0 2px 8px rgba(0,0,0,0.15)' : 'none',
      border: `2px solid`,
      borderColor: resolvedColor,
    };
    return (
      <Card ref={setNodeRef} style={style}>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
            <span {...attributes} {...listeners} style={{ cursor: 'grab', marginRight: 8, display: 'flex', alignItems: 'center' }}>
              <DragIndicatorIcon fontSize="small" color="disabled" />
            </span>
            <Typography variant="subtitle1" fontWeight={600}>{task.name}</Typography>
          </Box>
          <Typography variant="body2" color="text.secondary">{task.description || ''}</Typography>
          <Typography
            variant="caption"
            style={{
              display: 'block',
              marginTop: 4,
              fontWeight: 700,
              color:
                task.status === 'COMPLETE' ? '#388e3c' :
                task.status === 'BLOCKED' ? '#d32f2f' :
                task.status === 'IN_PROGRESS' ? '#1976d2' :
                '#222'
            }}
          >
            Status: {task.status ? (task.status.replace(/_/g, ' ').toLowerCase().replace(/^[a-z]/, c => c.toUpperCase())) : 'Not started'}
          </Typography>
          <Typography variant="caption" color="text.secondary" style={{ display: 'block', marginTop: 4 }}>
            Days Estimate: {task.dayEstimate != null ? task.dayEstimate : 'N/A'}
          </Typography>
          {assignedWorker ? (
            <Stack direction="row" alignItems="center" spacing={1} sx={{ mt: 1, mb: 1 }}>
              <Avatar sx={{ width: 24, height: 24, bgcolor: resolvedColor }}>
                <PersonIcon fontSize="small" />
              </Avatar>
              <Typography variant="body2">{assignedWorker.name}</Typography>
            </Stack>
          ) : (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1, mb: 1 }}>unassigned</Typography>
          )}
          <Divider sx={{ my: 1 }} />
          <Button size="small" startIcon={<EditIcon />} onClick={e => { e.stopPropagation(); handleEditOpen(task); }} tabIndex={0}>Edit</Button>
          <Button size="small" startIcon={<AssignmentIndIcon />} onClick={e => { e.stopPropagation(); handleAssignOpen(task.taskId); }} tabIndex={0}>Assign</Button>
          <Button size="small" color="primary" onClick={e => { e.stopPropagation(); handleMoveTaskOpen(task.taskId, currentTrackId); }} tabIndex={0}>Move</Button>
          <Button size="small" color="error" startIcon={<DeleteIcon />} onClick={e => { e.stopPropagation(); handleDeleteTask(task.taskId); }} tabIndex={0}>Delete</Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <Box>
      <AppBar position="static" color="primary">
        <Toolbar>
          {onBack && (
            <Button color="inherit" onClick={onBack} sx={{ mr: 2 }}>
              &larr; Back
            </Button>
          )}
          <Typography variant="h5" sx={{ flexGrow: 1, fontWeight: 700 }}>
            AkariPM {projectName ? `- ${projectName}` : ''}
          </Typography>
          <Button color="inherit" startIcon={<AddIcon />} onClick={handleAddTaskOpen}>Add Task</Button>
          <Button color="inherit" startIcon={<PlaylistAddIcon />} onClick={handleAddTrackOpen}>Add Track</Button>
          <Button color="inherit" startIcon={<AssignmentIndIcon />} onClick={handleAddWorkerOpen}>Add Team Member</Button>
        </Toolbar>
      </AppBar>
      <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
        {/* Workers Row */}
        {project.workers && project.workers.length > 0 && (
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 4, p: 2, borderRadius: 2, background: '#f5f7fa', boxShadow: 1 }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 600, mr: 2 }}>Team Members:</Typography>
            <Stack direction="row" spacing={2}>
              {project.workers.map(worker => (
                <Stack key={worker.userId} direction="row" alignItems="center" spacing={1}>
                  <Avatar sx={{ bgcolor: 'primary.main', width: 32, height: 32 }}>
                    <PersonIcon fontSize="small" />
                  </Avatar>
                  <Typography variant="body2">{worker.name}</Typography>
                </Stack>
              ))}
            </Stack>
          </Box>
        )}
        <Stack spacing={4}>
          {/* Horizontal tracks */}
          {otherTracks.map(track => {
            // Determine if calculatedDueDate is after desiredDueDate (target)
            let isLate = false;
            if (track.calculatedDueDate && track.desiredDueDate) {
              try {
                const calcDate = new Date(track.calculatedDueDate);
                const targetDate = new Date(track.desiredDueDate);
                isLate = calcDate > targetDate;
              } catch (e) {
                isLate = false;
              }
            }
            // Set border color: red if late, green if on track, gray if no target
            let borderColor = '#bdbdbd';
            if (track.calculatedDueDate && track.desiredDueDate) {
              borderColor = isLate ? '#e53935' : '#43a047';
            }
            return (
              <Paper
                key={track.trackId}
                variant="outlined"
                sx={{
                  borderColor: '#e0e0e0',
                  borderWidth: 2,
                  mb: 2,
                  p: 2,
                  background: '#fafbfc'
                }}
              >
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 1, minHeight: 56 }}>
                  <Typography variant="h6" sx={{ fontWeight: 600 }}>
                    {track.trackName}
                  </Typography>
                  <Button
                    size="small"
                    startIcon={<EditIcon />}
                    sx={{ ml: 2 }}
                    onClick={() => handleEditTrackOpen(track)}
                  >
                    Edit Track
                  </Button>
                  <Button
                    size="small"
                    color="error"
                    startIcon={<DeleteIcon />}
                    sx={{ ml: 1 }}
                    onClick={() => handleDeleteTrack(track.trackId)}
                  >
                    Delete
                  </Button>
                  <Box sx={{ flexGrow: 1 }} />
                  {/* Days remaining and due date on the right, centered vertically and horizontally */}
                  <Box
                    sx={{
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      justifyContent: 'center',
                      minWidth: 220,
                      height: '56px',
                      textAlign: 'center',
                      alignSelf: 'stretch',
                      mb: 3,
                    }}
                  >
                    <Paper
                      elevation={3}
                      sx={{
                        border: '2px solid',
                        borderColor: borderColor,
                        borderRadius: 2,
                        padding: '18px 24px 28px 24px',
                        background: '#f8fafc',
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'flex-start',
                        minWidth: 180,
                        mb: 1,
                        textAlign: 'left',
                        boxShadow: 3,
                        transition: 'border-color 0.2s, box-shadow 0.2s',
                      }}
                    >
                      <Typography variant="body2" sx={{ fontWeight: 500, mb: 1 }}>
                        Calculated Days Left: {track.calculatedNumBusinessDaysRemaining != null ? track.calculatedNumBusinessDaysRemaining : 'N/A'}
                      </Typography>
                      <Typography variant="body2" sx={{ fontWeight: 500, mb: 1 }}>
                        Calculated Done Date: {track.calculatedDueDate ? new Date(track.calculatedDueDate).toLocaleDateString() : 'N/A'}
                      </Typography>
                      <Typography variant="body2" sx={{ fontWeight: 500 }}>
                        Target Due Date: {track.desiredDueDate ? new Date(track.desiredDueDate).toLocaleDateString() : 'not entered'}
                      </Typography>
                    </Paper>
                  </Box>
                </Box>
                <DndContext
                  collisionDetection={closestCenter}
                  onDragEnd={async (event) => {
                    const { active, over } = event;
                    if (!over || active.id === over.id) return;
                    const oldIndex = (track.tasks || []).findIndex(t => t.taskId === active.id);
                    const newIndex = (track.tasks || []).findIndex(t => t.taskId === over.id);
                    if (oldIndex === -1 || newIndex === -1) return;
                    try {
                      // Store original state for rollback if needed
                      const originalTracks = JSON.parse(JSON.stringify(project.tracks));

                      // Create a new array with the updated order for optimistic UI update
                      const updatedTracks = project.tracks.map(t => {
                        if (t.trackId === track.trackId) {
                          // Create a new array with the reordered tasks
                          const newTasks = [...t.tasks];
                          const [movedTask] = newTasks.splice(oldIndex, 1);
                          newTasks.splice(newIndex, 0, movedTask);
                          return { ...t, tasks: newTasks };
                        }
                        return t;
                      });

                      // Update UI immediately
                      setProject({
                        ...project,
                        tracks: updatedTracks
                      });

                      // Make API call in background
                      await axios.post(`${API_BASE}/tasks/changeOrder`, null, {
                        params: {
                          taskId: active.id,
                          newPosition: newIndex
                        }
                      });

                      // Refresh from server after delay to ensure consistency
                      setTimeout(async () => {
                        try {
                          const res = await axios.get(`${API_BASE}/details`);
                          setProject(res.data);
                        } catch (err) {
                          console.error("Background refresh failed:", err);
                          // Rollback to original state if API call fails
                          setProject({
                            ...project,
                            tracks: originalTracks
                          });
                        }
                      }, 1000);
                    } catch (err) {
                      console.error("Failed to reorder tasks:", err);
                    }
                  }}
                >
                  <SortableContext
                    items={(track.tasks || []).map(task => task.taskId)}
                    strategy={horizontalListSortingStrategy}
                  >
                    <Box sx={{ display: 'flex', flexDirection: 'row', overflowX: 'auto', pb: 2 }}>
                      {(track.tasks || []).map((task, idx) => {
                        const assignedWorker = (project.workers || []).find(w => w.userId && w.userId === task.assignedUserId);
                        return (
                          <SortableTask
                            key={task.taskId}
                            task={task}
                            index={idx}
                            handleEditOpen={handleEditOpen}
                            handleAssignOpen={handleAssignOpen}
                            getStatusColor={getStatusColor}
                            getStatusClass={getStatusClass}
                            assignedWorker={assignedWorker}
                            handleMoveTaskOpen={handleMoveTaskOpen}
                            currentTrackId={track.trackId}
                          />
                        );
                      })}
                    </Box>
                  </SortableContext>
                </DndContext>
              </Paper>
            );
          })}

          {/* Backlog track as vertical list at bottom */}
          {backlogTrack && (
            <Box>
              <Typography variant="h6" sx={{ mb: 1, fontWeight: 600 }}>{backlogTrack.trackName}</Typography>
              <Paper variant="outlined" sx={{ p: 2, bgcolor: 'grey.100' }}>
                <Stack spacing={2}>
                  {(backlogTrack.tasks || []).map(task => {
                    const assignedWorker = (project.workers || []).find(w => w.userId && w.userId === task.assignedUserId);
                    return (
                      <Card key={task.taskId} className={`task ${getStatusClass(task.status)}`} sx={{ bgcolor: 'background.paper', boxShadow: 1, border: '2px solid', borderColor: getStatusColor(task.status), transition: 'border-color 0.2s' }}>
                        <CardContent>
                          <Typography variant="subtitle1" fontWeight={600}>{task.name}</Typography>
                          <Typography variant="body2" color="text.secondary">{task.description || ''}</Typography>
                          <Typography
                            variant="caption"
                            style={{
                              display: 'block',
                              marginTop: 4,
                              fontWeight: 700,
                              color:
                                task.status === 'COMPLETE' ? '#388e3c' :
                                task.status === 'BLOCKED' ? '#d32f2f' :
                                task.status === 'IN_PROGRESS' ? '#1976d2' :
                                '#222'
                            }}
                          >
                            Status: {task.status ? (task.status.replace(/_/g, ' ').toLowerCase().replace(/^[a-z]/, c => c.toUpperCase())) : 'Not started'}
                          </Typography>
                          <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 0.5 }}>
                            Days Estimate: {task.dayEstimate != null ? task.dayEstimate : 'N/A'}
                          </Typography>
                          {assignedWorker ? (
                            <Stack direction="row" alignItems="center" spacing={1} sx={{ mt: 1, mb: 1 }}>
                              <Avatar sx={{ width: 24, height: 24, bgcolor: 'primary.light' }}>
                                <PersonIcon fontSize="small" />
                              </Avatar>
                              <Typography variant="body2">{assignedWorker.name}</Typography>
                            </Stack>
                          ) : (
                            <Typography variant="body2" color="text.secondary" sx={{ mt: 1, mb: 1 }}>unassigned</Typography>
                          )}
                          <Divider sx={{ my: 1 }} />
                          <Button size="small" startIcon={<EditIcon />} onClick={() => handleEditOpen(task)}>Edit</Button>
                          <Button size="small" startIcon={<AssignmentIndIcon />} onClick={() => handleAssignOpen(task.taskId)}>Assign</Button>
                          <Button size="small" color="primary" onClick={() => handleMoveTaskOpen(task.taskId, backlogTrack.trackId)}>Move</Button>
                          <Button size="small" color="error" startIcon={<DeleteIcon />} onClick={() => handleDeleteTask(task.taskId)}>Delete</Button>
                        </CardContent>
                      </Card>
                    );
                  })}
                </Stack>
              </Paper>
            </Box>
          )}
        </Stack>
      </Container>

      {/* Add Task Dialog */}
      <Dialog open={addTaskOpen} onClose={handleAddTaskClose}>
        <DialogTitle>Add Task</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            name="name"
            label="Task Name"
            fullWidth
            value={newTask.name}
            onChange={handleAddTaskChange}
          />
          <TextField
            margin="dense"
            name="description"
            label="Description"
            fullWidth
            value={newTask.description}
            onChange={handleAddTaskChange}
          />
          <TextField
            select
            margin="dense"
            name="trackId"
            label="Track"
            fullWidth
            SelectProps={{ native: true }}
            value={newTask.trackId}
            onChange={handleAddTaskChange}
          >
            <option value="" disabled>Select track</option>
            {otherTracks.concat(backlogTrack ? [backlogTrack] : []).map(track => (
              <option key={track.trackId} value={track.trackId}>{track.trackName}</option>
            ))}
          </TextField>
          <TextField
            margin="dense"
            name="dayEstimate"
            label="Days Estimate"
            type="number"
            fullWidth
            required
            value={newTask.dayEstimate}
            onChange={handleAddTaskChange}
            inputProps={{ min: 1 }}
          />
          {addTaskError && <Typography color="error" variant="body2">{addTaskError}</Typography>}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleAddTaskClose}>Cancel</Button>
          <Button onClick={handleAddTaskSubmit} variant="contained">Add</Button>
        </DialogActions>
      </Dialog>

      {/* Add Track Dialog */}
      <Dialog open={addTrackOpen} onClose={handleAddTrackClose}>
        <DialogTitle>Add Track</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            name="trackName"
            label="Track Name"
            fullWidth
            value={newTrack.trackName}
            onChange={handleAddTrackChange}
          />
          {addTrackError && <Typography color="error" variant="body2">{addTrackError}</Typography>}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleAddTrackClose}>Cancel</Button>
          <Button onClick={handleAddTrackSubmit} variant="contained">Add</Button>
        </DialogActions>
      </Dialog>

      {/* Add Worker Dialog */}
      <Dialog open={addWorkerOpen} onClose={handleAddWorkerClose}>
        <DialogTitle>Add Worker</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            name="name"
            label="Worker Name"
            fullWidth
            value={newWorker.name}
            onChange={handleAddWorkerChange}
          />
          {addWorkerError && <Typography color="error" variant="body2">{addWorkerError}</Typography>}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleAddWorkerClose}>Cancel</Button>
          <Button onClick={handleAddWorkerSubmit} variant="contained">Add</Button>
        </DialogActions>
      </Dialog>

      {/* Assign Worker Dialog */}
      <Dialog open={assignDialogOpen} onClose={handleAssignClose}>
        <DialogTitle>Assign Worker</DialogTitle>
        <DialogContent>
          <TextField
            select
            margin="dense"
            name="workerId"
            label="Worker"
            fullWidth
            SelectProps={{ native: true }}
            value={assignWorkerId}
            onChange={handleAssignWorkerChange}
          >
            <option value="" disabled>Select worker</option>
            {(project.workers || []).map(worker => (
              <option key={worker.userId} value={worker.userId}>{worker.name}</option>
            ))}
          </TextField>
          {assignError && <Typography color="error" variant="body2">{assignError}</Typography>}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleAssignClose}>Cancel</Button>
          <Button onClick={handleAssignSubmit} variant="contained">Assign</Button>
        </DialogActions>
      </Dialog>

      {/* Edit Task Dialog */}
      <Dialog open={editTaskOpen} onClose={handleEditClose}>
        <DialogTitle>Edit Task</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            name="name"
            label="Task Name"
            fullWidth
            value={editTask?.name || ''}
            onChange={handleEditChange}
          />
          <TextField
            margin="dense"
            name="description"
            label="Description"
            fullWidth
            value={editTask?.description || ''}
            onChange={handleEditChange}
          />
          <TextField
            margin="dense"
            name="status"
            label="Status"
            select
            fullWidth
            value={editTask?.status || ''}
            onChange={handleEditChange}
            SelectProps={{ native: false }}
          >
            <MenuItem value="">Not started</MenuItem>
            <MenuItem value="IN_PROGRESS">In progress</MenuItem>
            <MenuItem value="COMPLETE">Complete</MenuItem>
            <MenuItem value="BLOCKED">Blocked</MenuItem>
          </TextField>
          {editTaskError && <Typography color="error" variant="body2">{editTaskError}</Typography>}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleEditClose}>Cancel</Button>
          <Button onClick={handleEditSubmit} variant="contained">Save</Button>
        </DialogActions>
      </Dialog>

      {/* Edit Track Dialog */}
      <Dialog open={editTrackOpen} onClose={handleEditTrackClose}>
        <DialogTitle>Edit Track</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            name="trackName"
            label="Track Name"
            fullWidth
            value={editTrack?.trackName || ''}
            onChange={handleEditTrackChange}
          />
          <TextField
            margin="dense"
            name="desiredDueDate"
            label="Desired Due Date"
            type="date"
            fullWidth
            InputLabelProps={{ shrink: true }}
            value={editTrack?.desiredDueDate || ''}
            onChange={handleEditTrackChange}
          />
          {editTrackError && <Typography color="error" variant="body2">{editTrackError}</Typography>}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleEditTrackClose}>Cancel</Button>
          <Button onClick={handleEditTrackSubmit} variant="contained">Save</Button>
        </DialogActions>
      </Dialog>

      {/* Move Task Dialog */}
      {moveTaskDialogOpen && (
        <Dialog open={moveTaskDialogOpen} onClose={handleMoveTaskClose}>
          <DialogTitle>Move Task to Another Track</DialogTitle>
          <DialogContent>
            <TextField
              select
              label="Select Track"
              value={moveTaskNewTrackId}
              onChange={handleMoveTaskChange}
              fullWidth
              margin="normal"
            >
              {tracks.filter(t => t.trackId !== moveTaskCurrentTrackId).map(track => (
                <MenuItem key={track.trackId} value={track.trackId}>{track.trackName}</MenuItem>
              ))}
            </TextField>
            {moveTaskError && <Typography color="error">{moveTaskError}</Typography>}
          </DialogContent>
          <DialogActions>
            <Button onClick={handleMoveTaskClose}>Cancel</Button>
            <Button onClick={handleMoveTaskSubmit} variant="contained">Move</Button>
          </DialogActions>
        </Dialog>
      )}
    </Box>
  );
}

export default ProjectBoard;

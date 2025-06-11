import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { Grid, Card, CardContent, Typography, Button, Box, Dialog, DialogTitle, DialogContent, DialogActions, TextField, CircularProgress } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useNavigate } from 'react-router-dom';

function ProjectLanding() {
  const navigate = useNavigate();
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);
  const [newProjectName, setNewProjectName] = useState('');
  const [createError, setCreateError] = useState('');
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    axios.get('http://localhost:8081/akari/project')
      .then(res => {
        setProjects(res.data || []);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  const handleSelectProject = (project) => {
    navigate(`/project/${project.projectId}`);
  };

  const handleCreate = async () => {
    if (!newProjectName.trim()) {
      setCreateError('Project name is required');
      return;
    }
    setCreating(true);

    // Generate a temporary ID for optimistic UI
    const tempProjectId = `temp-${Date.now()}`;

    // Create an optimistic project object
    const optimisticProject = {
      projectId: tempProjectId,
      projectName: newProjectName.trim()
    };

    // Add optimistic project to the list immediately
    setProjects([...projects, optimisticProject]);

    // Close the dialog
    setCreateOpen(false);

    try {
      // Make the actual API call in the background
      const response = await axios.post(`http://localhost:8081/akari/project/${encodeURIComponent(newProjectName.trim())}`);

      // Reset state
      setNewProjectName('');
      setCreateError('');
      setCreating(false);

      // Refresh project list from server to get the real ID
      const res = await axios.get('http://localhost:8081/akari/project');

      // Update projects with accurate data from server
      setProjects(res.data || []);
    } catch (e) {
      // If there's an error, remove the optimistic project
      setProjects(projects.filter(p => p.projectId !== tempProjectId));
      setCreateError('Failed to create project');
      setCreating(false);

      // Re-open the dialog to show the error
      setCreateOpen(true);
    }
  };

  return (
    <Box sx={{ mt: 6 }}>
      <Typography variant="h4" align="center" sx={{ mb: 4, fontWeight: 700 }}>
        Select a Project
      </Typography>
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}>
          <CircularProgress />
        </Box>
      ) : (
        <Grid container spacing={4} justifyContent="center">
          {projects.map(project => (
            <Grid item key={project.projectId}>
              <Card
                sx={{ minWidth: 220, minHeight: 120, cursor: 'pointer', transition: 'box-shadow 0.2s', '&:hover': { boxShadow: 6 } }}
                onClick={() => handleSelectProject(project)}
              >
                <CardContent>
                  <Typography variant="h6" sx={{ fontWeight: 600 }}>{project.projectName}</Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                    Project ID: {project.projectId}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
          <Grid item>
            <Card
              sx={{
                minWidth: 220,
                minHeight: 120,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                cursor: 'pointer',
                border: '2px dashed #90caf9',
                color: '#1976d2',
                transition: 'box-shadow 0.2s',
                '&:hover': { boxShadow: 6, background: '#e3f2fd' }
              }}
              onClick={() => setCreateOpen(true)}
            >
              <CardContent sx={{ textAlign: 'center' }}>
                <AddIcon sx={{ fontSize: 40 }} />
                <Typography variant="subtitle1" sx={{ mt: 1 }}>Create New Project</Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      <Dialog open={createOpen} onClose={() => setCreateOpen(false)}>
        <DialogTitle>Create New Project</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Project Name"
            fullWidth
            value={newProjectName}
            onChange={e => setNewProjectName(e.target.value)}
            disabled={creating}
          />
          {createError && <Typography color="error" variant="body2">{createError}</Typography>}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateOpen(false)} disabled={creating}>Cancel</Button>
          <Button onClick={handleCreate} variant="contained" disabled={creating}>
            {creating ? <CircularProgress size={20} /> : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

export default ProjectLanding;

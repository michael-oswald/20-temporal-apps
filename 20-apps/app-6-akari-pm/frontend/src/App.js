import './App.css';
import ProjectBoard from './ProjectBoard';
import ProjectLanding from './ProjectLanding';
import { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';

function ProjectBoardWrapper() {
  const { projectId } = useParams();
  const navigate = useNavigate();
  const [projectName, setProjectName] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    // Fetch project details to get the project name
    axios.get(`http://localhost:8081/project/${projectId}/details`)
      .then(res => {
        setProjectName(res.data.projectName || 'Project');
        setLoading(false);
      })
      .catch(err => {
        console.error("Error fetching project details:", err);
        setError("Could not load project. It may not exist.");
        setLoading(false);
      });
  }, [projectId]);

  const handleBack = () => {
    navigate('/');
  };

  if (loading) return <div>Loading project...</div>;
  if (error) return <div>{error} <button onClick={handleBack}>Back to Projects</button></div>;

  return (
    <ProjectBoard
      projectId={projectId}
      projectName={projectName}
      onBack={handleBack}
    />
  );
}

function App() {
  return (
    <Router>
      <div className="app-container">
        <div className="app-content">
          <Routes>
            <Route path="/" element={<ProjectLanding />} />
            <Route path="/project/:projectId" element={<ProjectBoardWrapper />} />
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </div>
        <footer className="footer">
          Made by Michael Oswald |
          <a href="https://www.youtube.com/@michael-oswald" target="_blank" rel="noopener noreferrer"> YouTube</a> |
          <a href="https://www.linkedin.com/in/mioswald/" target="_blank" rel="noopener noreferrer"> LinkedIn</a>
        </footer>
      </div>
    </Router>
  );
}

export default App;

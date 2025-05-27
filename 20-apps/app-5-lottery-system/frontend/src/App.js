import React, { useState, useEffect } from 'react';

// use this when deploying outside of local:
const API_BASE = '/lottery';
//const API_BASE = 'http://localhost:8081/lottery';
const DAILY_LIMIT = 500;

const Spinner = () => (
    <div style={{
        display: 'flex', justifyContent: 'center', alignItems: 'center', height: 60
    }}>
        <div style={{
            border: '4px solid #e0e0e0',
            borderTop: '4px solid #0078ff',
            borderRadius: '50%',
            width: 36,
            height: 36,
            animation: 'spin 1s linear infinite'
        }} />
        <style>
            {`@keyframes spin { 100% { transform: rotate(360deg); } }`}
        </style>
    </div>
);

const App = () => {
    const [numWinners, setNumWinners] = useState('');
    const [participants, setParticipants] = useState('');
    const [loading, setLoading] = useState(false);
    const [winners, setWinners] = useState([]);
    const [error, setError] = useState('');
    const [submitted, setSubmitted] = useState(false);
    const [remaining, setRemaining] = useState(null);
    const [backendType, setBackendType] = useState('simple');

    const fetchRemaining = async () => {
        try {
            const res = await fetch(`${API_BASE}/remaining`);
            const data = await res.json();
            setRemaining(data);
        } catch {
            setRemaining(null);
        }
    };

    useEffect(() => {
        if (backendType === 'temporal') {
            fetchRemaining();
        }
    }, [backendType]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setWinners([]);
        setSubmitted(true);

        if (!numWinners || isNaN(numWinners) || parseInt(numWinners) < 1) {
            setError('Please enter a valid number of winners.');
            setSubmitted(false);
            return;
        }
        if (!participants.trim()) {
            setError('Please enter at least one participant.');
            setSubmitted(false);
            return;
        }

        setLoading(true);

        try {
            const endpoint = backendType === 'simple' ? '/enterSimple' : '/enter';
            const enterRes = await fetch(`${API_BASE}${endpoint}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    numWinners: parseInt(numWinners, 10),
                    csvUserIds: participants
                })
            });
            if (!enterRes.ok) {
                const msg = await enterRes.text();
                throw new Error(msg || 'Failed to enter lottery');
            }
            const data = await enterRes.json();
            setWinners(Array.isArray(data) ? data : []);
            if (backendType === 'temporal') {
                fetchRemaining();
            }
        } catch (err) {
            setError(err.message || 'Something went wrong');
        } finally {
            setLoading(false);
            setSubmitted(false);
        }
    };

    const handleExport = () => {
        if (!winners.length) return;
        const csvContent = winners.join('\n');
        const blob = new Blob([csvContent], { type: 'text/csv' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'winners.csv';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    };

    const handleReset = () => {
        setNumWinners('');
        setParticipants('');
        setWinners([]);
        setError('');
        setSubmitted(false);
    };

    // Responsive styles
    const responsiveContainer = {
        background: '#fff',
        borderRadius: 18,
        boxShadow: '0 4px 32px rgba(0,0,0,0.08)',
        padding: 40,
        minWidth: 0,
        maxWidth: 400,
        width: '100%',
        marginTop: 60,
        boxSizing: 'border-box'
    };

    return (
        <div style={{
            minHeight: '100vh',
            background: 'linear-gradient(135deg, #f8fafc 0%, #e3e9f0 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontFamily: 'Roboto, sans-serif',
            position: 'relative',
            flexDirection: 'column'
        }}>
            {/* Responsive global styles */}
            <style>
                {`
                @media (max-width: 600px) {
                    .lottery-container {
                        padding: 18px 6px !important;
                        max-width: 98vw !important;
                        margin-top: 48px !important;
                    }
                    .top-note {
                        font-size: 14px !important;
                        padding: 10px 2px !important;
                    }
                    .footer-note {
                        font-size: 13px !important;
                        padding: 8px 0 !important;
                    }
                }
                `}
            </style>
            {/* Top algorithm note */}
            <div className="top-note" style={{
                width: '100%',
                background: '#fffbe6',
                color: '#8a6d3b',
                padding: '14px 0',
                fontSize: 16,
                fontWeight: 500,
                textAlign: 'center',
                borderBottom: '1px solid #ffe58f',
                position: 'absolute',
                top: 0,
                left: 0,
                zIndex: 200
            }}>
                The app randomly shuffles all participants and then selects the first N as winners, ensuring each participant has an equal chance.
            </div>
            <div className="lottery-container" style={responsiveContainer}>
                {/* Backend Toggle */}
                <div style={{
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    marginBottom: 18,
                    gap: 16,
                    flexWrap: 'wrap'
                }}>
                    <span style={{ fontWeight: 500, color: '#444' }}>Backend:</span>
                    <button
                        type="button"
                        onClick={() => setBackendType('simple')}
                        style={{
                            padding: '6px 18px',
                            borderRadius: 6,
                            border: backendType === 'simple' ? '2px solid #0078ff' : '1px solid #d0d7de',
                            background: backendType === 'simple' ? '#eaf4ff' : '#f8fafc',
                            color: backendType === 'simple' ? '#0078ff' : '#333',
                            fontWeight: 500,
                            cursor: 'pointer',
                            transition: 'all 0.2s'
                        }}
                    >
                        Simple
                    </button>
                    <button
                        type="button"
                        onClick={() => setBackendType('temporal')}
                        style={{
                            padding: '6px 18px',
                            borderRadius: 6,
                            border: backendType === 'temporal' ? '2px solid #0078ff' : '1px solid #d0d7de',
                            background: backendType === 'temporal' ? '#eaf4ff' : '#f8fafc',
                            color: backendType === 'temporal' ? '#0078ff' : '#333',
                            fontWeight: 500,
                            cursor: 'pointer',
                            transition: 'all 0.2s'
                        }}
                    >
                        Temporal
                    </button>
                </div>
                {/* Only show remaining lotteries for Temporal backend */}
                {backendType === 'temporal' && (
                    <div style={{
                        fontWeight: 600,
                        fontSize: 18,
                        color: '#0078ff',
                        marginBottom: 18,
                        textAlign: 'center'
                    }}>
                        {remaining !== null
                            ? `Temporal Lotteries available today: ${remaining}/${DAILY_LIMIT}`
                            : 'Loading remaining lotteries...'}
                    </div>
                )}
                <h2 style={{
                    fontWeight: 700,
                    fontSize: 28,
                    marginBottom: 24,
                    color: '#222',
                    textAlign: 'center'
                }}>🎲 Create a Lottery</h2>
                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
                    <label style={{ fontWeight: 500, color: '#444' }}>
                        Number of Winners
                        <input
                            type="number"
                            min="1"
                            max="1000"
                            value={numWinners}
                            onChange={e => setNumWinners(e.target.value)}
                            style={{
                                marginTop: 6,
                                padding: 10,
                                borderRadius: 8,
                                border: '1px solid #d0d7de',
                                fontSize: 16,
                                width: '100%',
                                boxSizing: 'border-box',
                                transition: 'border-color 0.2s',
                                outline: 'none'
                            }}
                            onFocus={e => e.target.style.borderColor = '#0078ff'}
                            onBlur={e => e.target.style.borderColor = '#d0d7de'}
                            disabled={loading || submitted}
                            required
                        />
                    </label>
                    <label style={{ fontWeight: 500, color: '#444' }}>
                        Participants (comma separated, max 1000)
                        <textarea
                            value={participants}
                            onChange={e => setParticipants(e.target.value)}
                            placeholder="e.g. john,Jen Hall,tommy@email.com"
                            rows={3}
                            style={{
                                marginTop: 6,
                                padding: 10,
                                borderRadius: 8,
                                border: '1px solid #d0d7de',
                                fontSize: 16,
                                width: '100%',
                                boxSizing: 'border-box',
                                resize: 'vertical',
                                transition: 'border-color 0.2s',
                                outline: 'none'
                            }}
                            onFocus={e => e.target.style.borderColor = '#0078ff'}
                            onBlur={e => e.target.style.borderColor = '#d0d7de'}
                            disabled={loading || submitted}
                            required
                        />
                    </label>
                    <button
                        type="submit"
                        style={{
                            marginTop: 10,
                            padding: '12px 0',
                            borderRadius: 8,
                            border: 'none',
                            background: 'linear-gradient(90deg, #0078ff 0%, #00c6fb 100%)',
                            color: '#fff',
                            fontWeight: 600,
                            fontSize: 18,
                            cursor: loading || submitted || (backendType === 'temporal' && remaining < 1) ? 'not-allowed' : 'pointer',
                            opacity: loading || submitted || (backendType === 'temporal' && remaining < 1) ? 0.7 : 1,
                            transition: 'opacity 0.2s, transform 0.2s',
                            boxShadow: '0 4px 8px rgba(0,0,0,0.1)'
                        }}
                        onMouseEnter={e => e.target.style.transform = 'scale(1.02)'}
                        onMouseLeave={e => e.target.style.transform = 'scale(1)'}
                        disabled={loading || submitted || (backendType === 'temporal' && remaining < 1)}
                    >
                        {loading ? 'Processing...' : 'Run Lottery'}
                    </button>
                </form>
                {loading && <Spinner />}
                {error && <div style={{ color: '#e00', marginTop: 18, fontWeight: 500 }}>{error}</div>}
                {winners.length > 0 && (
                    <div style={{
                        marginTop: 32,
                        background: '#f4f8fb',
                        borderRadius: 12,
                        padding: 20,
                        textAlign: 'center'
                    }}>
                        <h3 style={{ color: '#0078ff', marginBottom: 12 }}>🏆 Winners</h3>
                        <div style={{
                            display: 'grid',
                            gridTemplateColumns: 'repeat(auto-fit, minmax(100px, 1fr))',
                            gap: 12,
                            marginBottom: 16
                        }}>
                            {winners.map((w, i) => (
                                <div key={i} style={{
                                    background: '#fff',
                                    borderRadius: 8,
                                    boxShadow: '0 1px 4px rgba(0,0,0,0.04)',
                                    padding: 10,
                                    fontWeight: 600,
                                    color: '#222',
                                    textAlign: 'center'
                                }}>{w}</div>
                            ))}
                        </div>
                        <div>
                            <button
                                onClick={handleExport}
                                style={{
                                    padding: '8px 18px',
                                    borderRadius: 6,
                                    border: 'none',
                                    background: 'linear-gradient(90deg, #00c6fb 0%, #0078ff 100%)',
                                    color: '#fff',
                                    fontWeight: 500,
                                    fontSize: 15,
                                    cursor: 'pointer',
                                    marginRight: 10,
                                    transition: 'background 0.2s'
                                }}
                            >
                                Export Winners
                            </button>
                            <button
                                onClick={handleReset}
                                style={{
                                    padding: '8px 18px',
                                    borderRadius: 6,
                                    border: 'none',
                                    background: '#e0e0e0',
                                    color: '#333',
                                    fontWeight: 500,
                                    fontSize: 15,
                                    cursor: 'pointer',
                                    transition: 'background 0.2s'
                                }}
                            >
                                Clear Results
                            </button>
                        </div>
                    </div>
                )}
            </div>
            {/* Footer */}
            <footer className="footer-note" style={{
                position: 'fixed',
                left: 0,
                bottom: 0,
                width: '100%',
                background: '#f8fafc',
                color: '#444',
                textAlign: 'center',
                padding: '12px 0 32px 0',
                fontSize: 15,
                borderTop: '1px solid #e0e0e0',
                zIndex: 100
            }}>
                Made by Michael Oswald &nbsp;|&nbsp;
                <a
                    href="https://github.com/michael-oswald/20-temporal-apps/tree/main/20-apps/app-5-lottery-system"
                    target="_blank"
                    rel="noopener noreferrer"
                    style={{ color: '#0078ff', textDecoration: 'underline' }}
                >
                    View Code on Github
                </a>
                <div style={{
                    marginTop: 10,
                    color: '#888',
                    fontSize: 13,
                    fontStyle: 'italic'
                }}>
                    This app is a demo application for demonstration purposes only and is not intended for real production use.
                </div>
            </footer>
        </div>
    );
};

export default App;
import React, {useState, useEffect} from 'react';
import SouthwestSeatMap from './SouthwestSeatMap';

const API_BASE = 'http://localhost:8081/booking'; // adjust if needed

const App = () => {
    const [userId, setUserId] = useState('');
    const [seat, setSeat] = useState(null);
    const [status, setStatus] = useState('');
    const [seatStatus, setSeatStatus] = useState({});
    const [availableSeats, setAvailableSeats] = useState([]);

    useEffect(() => {
        const fetchSeats = async () => {
            const res = await fetch(`${API_BASE}/seats/available`, {method: 'GET'});
            const seats = await res.json();
            setAvailableSeats(seats);
        };

        fetchSeats();
        const interval = setInterval(fetchSeats, 2000);
        return () => clearInterval(interval);
    }, []);

    useEffect(() => {
        const fetchSeatStatus = async () => {
            const res = await fetch(`http://localhost:8081/seats/status`);
            const data = await res.json();
            setSeatStatus(data);
        };

        fetchSeatStatus();
        const interval = setInterval(fetchSeatStatus, 2000);
        return () => clearInterval(interval);
    }, []);

    const handleBook = async () => {
        if (!userId) return alert('Enter user ID');
        try {
            const res = await fetch(`${API_BASE}/book/${userId}`, {method: 'POST'});
            if (res.status === 409) {
                setStatus('Booking failed: Plane is full. No seats available.');
                setSeat(null);
                return;
            }
            const text = await res.text();
            setStatus('Booking requested. Check seat map for confirmation.');
            setSeat('Assigned after booking');
        } catch (err) {
            setStatus('Booking failed');
        }
    };

    return (
        <div style={{padding: 30, fontFamily: 'sans-serif'}}>
            <h2>✈️ Airline Seat Booking</h2>

            <input
                type="text"
                value={userId}
                onChange={e => setUserId(e.target.value)}
                placeholder="Enter user ID"
                style={{padding: 8, width: 200, marginRight: 10}}
            />

            <div style={{marginTop: 20}}>
                <button onClick={handleBook}>Book Seat</button>
            </div>

            <div style={{marginTop: 30}}>
                <strong>Status:</strong> {status}<br/>
                <strong>Seat:</strong> {seat || 'N/A'}
            </div>
            <div style={{marginTop: 20}}>
                <strong>Available Seats:</strong>&nbsp;
                {availableSeats.length === 0 ? "No seats left" : availableSeats.length}
                <br/><br/>
            </div>
            <div style={{display: 'flex', gap: 16, alignItems: 'center', justifyContent: 'center', marginBottom: 16}}>
                <span style={{display: 'flex', alignItems: 'center', gap: 4}}>
                    <span style={{
                        width: 20,
                        height: 20,
                        background: '#d4f7dc',
                        border: '1px solid #999',
                        borderRadius: 4,
                        display: 'inline-block'
                    }}></span>
                    Available
                </span>
                <span style={{display: 'flex', alignItems: 'center', gap: 4}}>
                    <span style={{
                        width: 20,
                        height: 20,
                        background: '#ffe29a',
                        border: '1px solid #999',
                        borderRadius: 4,
                        display: 'inline-block'
                    }}></span>
                    Booking in Progress
                </span>
                <span style={{display: 'flex', alignItems: 'center', gap: 4}}>
                    <span style={{
                        width: 20,
                        height: 20,
                        background: '#ec0606',
                        border: '1px solid #999',
                        borderRadius: 4,
                        display: 'inline-block'
                    }}></span>
                    Booked
                </span>
            </div>
            <div>
                <strong>Seat Map:</strong>
                <div style={{marginTop: 20}}>
                    <SouthwestSeatMap seatStatus={seatStatus} allBooked={availableSeats.length === 0} />
                </div>
            </div>
        </div>
    );
};

export default App;
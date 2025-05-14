import React, {useState, useEffect} from 'react';
import SouthwestSeatMap from './SouthwestSeatMap';


const API_BASE = 'http://localhost:8081/booking'; // adjust if needed

const App = () => {
    const [userId, setUserId] = useState('');
    const [seat, setSeat] = useState(null);
    const [status, setStatus] = useState('');
    const [timer, setTimer] = useState(0); // in seconds
    const [seatStatus, setSeatStatus] = useState({});
    const [availableSeats, setAvailableSeats] = useState([]);

    useEffect(() => {
        let interval;
        if (timer > 0) {
            interval = setInterval(() => setTimer(t => t - 1), 1000);
        }
        return () => clearInterval(interval);
    }, [timer]);

    useEffect(() => {
        const fetchSeats = async () => {
            //const res = await fetch(`${API_BASE.replace('/booking', '')}/seats/available`);
            const res = await fetch(`${API_BASE}/seats/available`, {method: 'GET'});
            const seats = await res.json();
            setAvailableSeats(seats);
        };

        fetchSeats(); // initial fetch

        const interval = setInterval(fetchSeats, 5000); // poll every 5s
        return () => clearInterval(interval);
    }, []);

    useEffect(() => {
        const fetchSeatStatus = async () => {
            const res = await fetch(`http://localhost:8081/seats/status`);
            const data = await res.json();
            setSeatStatus(data); // Expects format: { "1A": "CONFIRMED", "2B": "HELD", ... }
        };

        fetchSeatStatus();
        const interval = setInterval(fetchSeatStatus, 3000);
        return () => clearInterval(interval);
    }, []);

    const formatTime = (seconds) => {
        const m = String(Math.floor(seconds / 60)).padStart(2, '0');
        const s = String(seconds % 60).padStart(2, '0');
        return `${m}:${s}`;
    };

    const handleStartBooking = async () => {
        if (!userId) return alert('Enter user ID');
        try {
            const res = await fetch(`${API_BASE}/start/${userId}`, {method: 'POST'});
            const text = await res.text();
            setStatus('Seat reserved. Waiting for payment...');
            setSeat('(assigned after confirmation)'); // or update if API returns it
            setTimer(5 * 60); // 15 minutes
        } catch (err) {
            setStatus('Booking failed');
        }
    };

    const handlePayment = async () => {
        try {
            const res = await fetch(`${API_BASE}/pay/${userId}`, {method: 'POST'});
            const text = await res.text();
            setStatus('Payment received. Booking confirmed!');
            setSeat('Seat confirmed (check DB for real value)');
            setTimer(0);
        } catch (err) {
            setStatus('Payment failed');
        }
    };

    const handleCancel = async () => {
        try {
            const res = await fetch(`${API_BASE}/cancel/${userId}`, {method: 'POST'});
            const text = await res.text();
            setStatus('Booking cancelled.');
            setSeat(null);
            setTimer(0);
        } catch (err) {
            setStatus('Cancel failed');
        }
    };

    return (
        <div style={{padding: 30, fontFamily: 'sans-serif'}}>
            <div style={{
                position: 'absolute',
                top: 16,
                right: 24,
                fontSize: 40,
                fontWeight: 'bold',
                background: 'rgba(255,255,255,0.95)',
                color: '#1a237e',
                padding: '12px 28px',
                borderRadius: 16,
                boxShadow: '0 2px 12px rgba(0,0,0,0.10)',
                letterSpacing: 2,
                zIndex: 10
            }}>
                ⏳ {timer > 0 ? formatTime(timer) : ''}
            </div>

            <h2>✈️ Airline Seat Booking</h2>

            <input
                type="text"
                value={userId}
                onChange={e => setUserId(e.target.value)}
                placeholder="Enter user ID"
                style={{padding: 8, width: 200, marginRight: 10}}
            />

            <div style={{marginTop: 20}}>
                <button onClick={handleStartBooking} style={{marginRight: 10}}>Start Booking</button>
                <button onClick={handlePayment} style={{marginRight: 10}}>Confirm Payment</button>
                <button onClick={handleCancel}>Cancel Booking</button>
            </div>

            <div style={{marginTop: 30}}>
                <strong>Status:</strong> {status}<br/>
                <strong>Seat:</strong> {seat || 'N/A'}
            </div>
            <div style={{marginTop: 20}}>
                <strong>Available Seats:</strong>&nbsp;
                {availableSeats.length === 0 ? "No seats left" : availableSeats.length}
                <br></br>
                <br></br>
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
                <strong>💺Seat Map:</strong>
                <div style={{marginTop: 20}}>
                    <SouthwestSeatMap seatStatus={seatStatus} allBooked={availableSeats.length === 0} />
                </div>
            </div>
        </div>
    );
};

export default App;

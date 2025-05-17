import React from 'react';
import './PlaneSeatMap.css';

const rows = Array.from({ length: 30 }, (_, i) => i + 1);
const seatLetters = ['A', 'B', 'C', 'D', 'E', 'F'];

const PlaneSeatMap = ({ seatStatus = {}, allBooked = false }) => {
    return (
        <div className="plane-outline">
            <div className="plane-cabin">
                {rows.map((row) => (
                    <div key={row} className="seat-row">
                        <div className="seat-group">
                            {['A', 'B', 'C'].map((letter) => {
                                const seatId = `${row}${letter}`;
                                const status = allBooked ? 'confirmed' : (seatStatus[seatId] || 'available');
                                return (
                                    <div key={seatId} className={`seat ${status.toLowerCase()}`}>
                                        {seatId}
                                    </div>
                                );
                            })}
                        </div>
                        <div className="aisle-gap" />
                        <div className="seat-group">
                            {['D', 'E', 'F'].map((letter) => {
                                const seatId = `${row}${letter}`;
                                const status = allBooked ? 'confirmed' : (seatStatus[seatId] || 'available');
                                return (
                                    <div key={seatId} className={`seat ${status.toLowerCase()}`}>
                                        {seatId}
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default PlaneSeatMap;
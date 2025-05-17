import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
import { Counter } from 'k6/metrics';

export let options = {
    stages: [
        { duration: '5s', target: 5 },
        { duration: '5s', target: 5 },
        { duration: '5s', target: 5 },
        { duration: '5s', target: 0 },
    ],

};

const BASE_URL = 'http://localhost:8081/booking';
export let denied = new Counter('denied_seats');

export default function () {
    const userId = uuidv4();

    // Start booking
    const startRes = http.post(`${BASE_URL}/book/${userId}`);
    if (!startRes || startRes.status === 409) {
        denied.add(1);
        console.log(`DENIED: ${userId}`);
        return;
    }
    check(startRes, {
        'booking submitted': (r) => r && r.status === 200,
    });
}
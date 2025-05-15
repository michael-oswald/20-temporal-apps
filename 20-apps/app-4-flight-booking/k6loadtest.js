import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
import { Counter } from 'k6/metrics';

const BASE_URL = 'http://localhost:8081/booking';
export let denied = new Counter('denied_seats');

export default function () {
    const userId = uuidv4();

    // Start booking
    const startRes = http.post(`${BASE_URL}/start/${userId}`);
    if (!startRes || startRes.status === 409) {
        denied.add(1);
        console.log(`DENIED: ${userId}`);
        return;
    }
    check(startRes, {
        'booking started or already running': (r) => r && r.status === 200,
    });

    sleep(Math.random() * 3);

    const payRes = http.post(`${BASE_URL}/pay/${userId}`);
    if (payRes) {
        check(payRes, {
            'payment received': (r) => r && r.status === 200,
        });
    }
}
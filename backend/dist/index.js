"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = __importDefault(require("express"));
const http_1 = __importDefault(require("http"));
const socket_io_1 = require("socket.io");
const cors_1 = __importDefault(require("cors"));
const path_1 = __importDefault(require("path"));
const routes_1 = __importDefault(require("./routes"));
const database_1 = require("./database");
const socket_1 = require("./socket");
const app = (0, express_1.default)();
const server = http_1.default.createServer(app);
const io = new socket_io_1.Server(server, {
    cors: {
        origin: '*',
        methods: ['GET', 'POST']
    }
});
const PORT = process.env.PORT || 3000;
app.use((0, cors_1.default)());
app.use(express_1.default.json());
// Serve uploaded media files statically
app.use('/uploads', express_1.default.static(path_1.default.resolve(__dirname, '../uploads')));
// Register routes
app.use('/api', routes_1.default);
// Start background task to purge expired status/stories (older than 24h)
function startStatusPurgeJob() {
    const ONE_HOUR = 60 * 60 * 1000;
    setInterval(async () => {
        try {
            const now = Date.now();
            const expiredStatuses = await database_1.dbOperations.all('SELECT id FROM status WHERE expiresAt < ?', [now]);
            if (expiredStatuses.length > 0) {
                console.log(`Purging ${expiredStatuses.length} expired statuses`);
                const ids = expiredStatuses.map(s => `'${s.id}'`).join(',');
                await database_1.dbOperations.run(`DELETE FROM status WHERE id IN (${ids})`);
                await database_1.dbOperations.run(`DELETE FROM status_views WHERE statusId IN (${ids})`);
            }
        }
        catch (err) {
            console.error('Failed to purge expired status updates:', err);
        }
    }, ONE_HOUR);
}
// Initialize database and start server
(0, database_1.initializeDatabase)()
    .then(() => {
    console.log('SQLite Database initialized successfully.');
    // Setup WebSockets
    (0, socket_1.setupSocketIO)(io);
    // Start background status purger
    startStatusPurgeJob();
    server.listen(PORT, () => {
        console.log(`Quick Chat backend server is running on port ${PORT}`);
    });
})
    .catch((err) => {
    console.error('Failed to initialize SQLite database:', err);
    process.exit(1);
});

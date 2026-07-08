import express from 'express';
import http from 'http';
import { Server } from 'socket.io';
import cors from 'cors';
import path from 'path';
import router from './routes';
import { initializeDatabase, dbOperations } from './database';
import { setupSocketIO } from './socket';

const app = express();
const server = http.createServer(app);
const io = new Server(server, {
  cors: {
    origin: '*',
    methods: ['GET', 'POST']
  }
});

const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

// Serve uploaded media files statically
app.use('/uploads', express.static(path.resolve(__dirname, '../uploads')));

// Register routes
app.use('/api', router);

// Start background task to purge expired status/stories (older than 24h)
function startStatusPurgeJob() {
  const ONE_HOUR = 60 * 60 * 1000;
  setInterval(async () => {
    try {
      const now = Date.now();
      const expiredStatuses = await dbOperations.all<{ id: string }>('SELECT id FROM status WHERE expiresAt < ?', [now]);
      
      if (expiredStatuses.length > 0) {
        console.log(`Purging ${expiredStatuses.length} expired statuses`);
        const ids = expiredStatuses.map(s => `'${s.id}'`).join(',');
        await dbOperations.run(`DELETE FROM status WHERE id IN (${ids})`);
        await dbOperations.run(`DELETE FROM status_views WHERE statusId IN (${ids})`);
      }
    } catch (err) {
      console.error('Failed to purge expired status updates:', err);
    }
  }, ONE_HOUR);
}

// Initialize database and start server
initializeDatabase()
  .then(() => {
    console.log('SQLite Database initialized successfully.');
    
    // Setup WebSockets
    setupSocketIO(io);
    
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

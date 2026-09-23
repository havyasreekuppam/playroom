/**
 * PlayRoom backend - in-memory rooms, no database.
 *
 * Events the server handles (client -> server):
 *   join_room    { gameId, playerName }   -> joins (or creates) the room for that game
 *   leave_room   {}                       -> leaves the current room
 *   send_message { text }                 -> chat to everyone in the room
 *
 * Events the server sends (server -> client):
 *   joined_room      { gameId, roomId, players, maxPlayers }
 *   room_update      { roomId, gameId, players: [{id, name}], maxPlayers }
 *   receive_message  { roomId, sender, text, sentAt }
 *   room_error       { code, message }    e.g. room full, no room, bad input
 *   rooms_snapshot   [{roomId, gameId, players, maxPlayers}]
 *                                    lobby-wide snapshot with live counts,
 *                                    sent on connect and on every change
 */
const express = require("express");
const http = require("http");
const { Server } = require("socket.io");

const app = express();
const server = http.createServer(app);
const io = new Server(server, { cors: { origin: "*" } });

const PORT = process.env.PORT || 3000;

/** Game definitions - must mirror the Android GameCatalog. */
const GAMES = {
  ludo: { name: "Ludo", maxPlayers: 8 },
  chess: { name: "Chess", maxPlayers: 4 },
  "quiz-battle": { name: "Quiz Battle", maxPlayers: 10 },
};

/**
 * In-memory room state: { [roomId]: { id, gameId, maxPlayers, players: Map<socketId, {name}> } }
 * One room per game keeps the MVP simple.
 */
const rooms = new Map();

function getOrCreateRoom(gameId) {
  let room = rooms.get(gameId);
  if (!room) {
    const game = GAMES[gameId];
    room = {
      id: `${gameId}-room`,
      gameId,
      maxPlayers: game.maxPlayers,
      players: new Map(), // socketId -> { name }
    };
    rooms.set(gameId, room);
  }
  return room;
}

function roomSnapshot(room) {
  return {
    roomId: room.id,
    gameId: room.gameId,
    maxPlayers: room.maxPlayers,
    players: Array.from(room.players.entries()).map(([id, p]) => ({
      id,
      name: p.name,
    })),
  };
}

function broadcastRoomUpdate(room) {
  io.to(room.id).emit("room_update", roomSnapshot(room));
}

/** Lobby-wide snapshot: every room, so clients can show live counts. */
function lobbySnapshot() {
  return Array.from(rooms.values()).map(roomSnapshot);
}

function broadcastLobby() {
  io.emit("rooms_snapshot", lobbySnapshot());
}

app.get("/", (_req, res) => {
  res.json({ status: "ok", service: "playroom-server" });
});

app.get("/rooms", (_req, res) => {
  res.json(
    Array.from(rooms.values()).map((room) => ({
      roomId: room.id,
      gameId: room.gameId,
      maxPlayers: room.maxPlayers,
      playerCount: room.players.size,
      players: Array.from(room.players.values()).map((p) => p.name),
    }))
  );
});

io.on("connection", (socket) => {
  console.log(`[conn] ${socket.id} connected`);

  // Snapshot of all rooms, useful right after connect.
  socket.emit("rooms_snapshot", lobbySnapshot());

  /** Sanitize a name: trim, cap length, fall back to "Player". */
  function safeName(raw, fallback) {
    const name = String(raw ?? "").trim().slice(0, 24);
    return name.length > 0 ? name : fallback;
  }

  socket.on("join_room", ({ gameId, playerName } = {}) => {
    const game = GAMES[gameId];
    if (!game) {
      socket.emit("room_error", { code: "UNKNOWN_GAME", message: `Unknown game: ${gameId}` });
      return;
    }

    // Leave any previous room first (one room per client).
    for (const [, room] of rooms) {
      if (room.players.has(socket.id)) {
        leaveRoom(socket, room);
      }
    }

    const room = getOrCreateRoom(gameId);

    if (room.players.size >= room.maxPlayers) {
      socket.emit("room_error", { code: "ROOM_FULL", message: `${room.id} is full (${room.maxPlayers} players)` });
      return;
    }

    const name = safeName(playerName, `Player-${socket.id.slice(0, 4)}`);
    room.players.set(socket.id, { name });
    socket.join(room.id);
    socket.data.roomId = room.id;

    socket.emit("joined_room", { ...roomSnapshot(room), yourName: name });
    broadcastRoomUpdate(room);
    broadcastLobby();
    console.log(`[join] ${name} (${socket.id}) -> ${room.id} (${room.players.size}/${room.maxPlayers})`);
  });

  socket.on("leave_room", () => {
    const room = findMyRoom();
    if (room) {
      leaveRoom(socket, room);
      socket.emit("left_room", { roomId: room.id });
    } else {
      socket.emit("room_error", { code: "NOT_IN_ROOM", message: "You are not in a room" });
    }
  });

  socket.on("send_message", ({ text } = {}) => {
    const room = findMyRoom();
    if (!room) {
      socket.emit("room_error", { code: "NOT_IN_ROOM", message: "Join a room before chatting" });
      return;
    }
    const message = String(text ?? "").trim().slice(0, 300);
    if (!message) {
      socket.emit("room_error", { code: "EMPTY_MESSAGE", message: "Message is empty" });
      return;
    }
    const payload = {
      roomId: room.id,
      sender: room.players.get(socket.id)?.name ?? "Player",
      text: message,
      sentAt: Date.now(),
    };
    io.to(room.id).emit("receive_message", payload);
  });

  socket.on("disconnect", (reason) => {
    const room = findMyRoom();
    if (room) {
      console.log(`[disc] ${socket.id} left ${room.id} (${reason})`);
      leaveRoom(socket, room);
    }
    console.log(`[conn] ${socket.id} disconnected: ${reason}`);
  });

  function findMyRoom() {
    const roomId = socket.data.roomId;
    if (!roomId) return null;
    const room = rooms.get(roomId.replace(/-room$/, ""));
    return room && room.players.has(socket.id) ? room : null;
  }
});

/** Remove a player from a room, delete empty rooms, notify the rest. */
function leaveRoom(socket, room) {
  room.players.delete(socket.id);
  socket.leave(room.id);
  socket.data.roomId = null;
  if (room.players.size === 0) {
    rooms.delete(room.gameId);
  } else {
    broadcastRoomUpdate(room);
  }
  // Counts changed (or the room vanished) - tell every connected lobby.
  broadcastLobby();
}

server.listen(PORT, () => {
  console.log(`PlayRoom server listening on http://localhost:${PORT}`);
});

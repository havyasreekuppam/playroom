# PlayRoom

A real-time gaming lobby for Android. Browse a neon-themed list of games
(Ludo, Chess, Quiz Battle), join a room with a display name, see players
come and go live, chat with the room, and leave cleanly — all powered by
Socket.IO with an in-memory Node.js backend (no database, no auth).

```
Android (Compose) ⇄ Socket.IO ⇄ Node.js + Express ⇄ Rooms ⇄ everyone in the room
```

## Screens

| Screen | What it does |
|---|---|
| **Lobby** | Searchable game cards (filter by name or category), connection status comes up on the room screen |
| **Room** | Enter display name → JOIN ROOM → live player list + count, room chat, LEAVE ROOM |

## Tech stack

- **Android:** Kotlin, Jetpack Compose (Material 3), ViewModel + StateFlow (simple MVVM), `socket.io-client`
- **Backend:** Node.js, Express, Socket.IO — all room state is in-memory `Map`s
- **Tests:** Node's built-in test runner style script using `socket.io-client` to simulate multiple phones

## Project layout

```
playroom/
├── app/                          # Android app
│   └── src/main/java/com/playroom/app/
│       ├── MainActivity.kt       # two-screen manual navigation (lobby ⇄ room)
│       ├── model/Game.kt         # Game data class + static catalog
│       ├── data/
│       │   ├── Models.kt         # ConnectionState, RoomState, ChatMessage
│       │   └── SocketRoomRepository.kt  # socket.io-client wrapped in Flows
│       └── ui/
│           ├── HomeScreen.kt     # lobby + search
│           ├── LobbyViewModel.kt # search/filter state
│           ├── RoomScreen.kt     # join, players, chat, leave
│           └── RoomViewModel.kt  # room session state
└── server/                       # Node.js backend
    ├── src/server.js             # Express + Socket.IO, in-memory rooms
    └── test/multi-client.test.js # multi-client integration test
```

## Socket.IO contract

**Client → server**

| Event | Payload | Notes |
|---|---|---|
| `join_room` | `{ gameId, playerName }` | one room per game; `ROOM_FULL` / `UNKNOWN_GAME` errors possible |
| `leave_room` | `{}` | server replies `left_room` |
| `send_message` | `{ text }` | max 300 chars; broadcast to the room |

**Server → client**

| Event | Payload |
|---|---|
| `joined_room` | `{ roomId, gameId, maxPlayers, players[], yourName }` |
| `room_update` | `{ roomId, gameId, maxPlayers, players[] }` — on every join/leave/disconnect |
| `receive_message` | `{ roomId, sender, text, sentAt }` |
| `left_room` | `{ roomId }` |
| `room_error` | `{ code, message }` — `ROOM_FULL`, `UNKNOWN_GAME`, `NOT_IN_ROOM`, `EMPTY_MESSAGE` |
| `rooms_snapshot` | `[{ roomId, gameId, players[], maxPlayers }]` — lobby-wide live counts, sent on connect and on every join/leave |

Rooms are deleted when the last player leaves. An abrupt disconnect is
treated exactly like leaving.

## Run the backend

```bash
cd server
npm install
npm start          # -> PlayRoom server listening on http://localhost:3000
```

Debug endpoints: `GET /` (health), `GET /rooms` (live room snapshot).

## Run the multi-client test

```bash
cd server
npm test           # starts against http://localhost:3000, server must be running
```

## Run the Android app

1. Open the project root in **Android Studio** and let Gradle sync.
2. Start the backend (`npm start` in `server/`).
3. Run the app on an **emulator** — it connects to `http://10.0.2.2:3000`
   (the emulator's alias for your machine's localhost).
4. On a **physical device**, change `DEFAULT_SERVER_URL` in
   `app/src/main/java/com/playroom/app/data/SocketRoomRepository.kt`
   to your computer's LAN IP, e.g. `http://192.168.1.20:3000`.

Open two emulators/devices to watch player counts and chat sync in real time.

## What is implemented

- ✅ Game catalog (Ludo, Chess, Quiz Battle) with max-player limits
- ✅ Lobby search (name/category, live filtering)
- ✅ MVVM: screens are stateless, ViewModels expose `StateFlow`
- ✅ Join room with display name; server-side capacity enforcement
- ✅ Live player list + count via `room_update` broadcasts
- ✅ Room chat (`send_message` / `receive_message`), own messages highlighted
- ✅ Leave room (button + system back) and disconnect cleanup
- ✅ Connection status indicator (connecting / connected / disconnected / failed)
- ✅ Error surfacing: room full, unknown game, empty message → Snackbar
- ✅ Multi-client integration test (12 assertions)
- ✅ Live lobby player counts (`rooms_snapshot` broadcast on every join/leave)
- ✅ Automatic room rejoin after a transient disconnect
- ✅ Socket lifecycle cleanup on screen teardown (no leaked connections)

## Limitations / not implemented

- No real gameplay — the MVP covers lobby/room presence and chat only.
- Room state is in-memory; restarting the server resets everything, and
  it does not scale beyond one process.
- No persistence, no auth; a server restart still wipes rooms (clients do
  auto-rejoin after a transient network drop, not after a restart).
- One room per game (everyone who joins Chess lands in the same room).
- The full end-to-end flow was verified with the automated multi-client
  socket test and Android builds; running the app itself on an emulator
  (`10.0.2.2:3000`) is the one manual check to do on your machine.

## Interview quick answers

**30 seconds:** *PlayRoom is a real-time gaming lobby. On Android I built a
Jetpack Compose UI with a simple MVVM setup — a lobby with searchable game
cards, then a room screen with a live player list and chat. All real-time
behavior runs over Socket.IO against a small Node.js + Express backend that
keeps rooms in memory. Clients emit `join_room`, `send_message`,
`leave_room`; the server broadcasts `room_update` and `receive_message` so
every phone sees players and messages instantly. Leaving and abrupt
disconnects both trigger cleanup, and a multi-client integration test
simulates three phones joining, chatting and dropping.*

**One minute:** *The goal was an end-to-end real-time MVP with no Firebase
and no database. The Android app is Compose + Material 3 with a deliberate
MVVM split: screens are stateless and collect state from ViewModels
(`StateFlow`), and all socket handling is hidden behind one repository class
that exposes flows — so the UI never touches socket internals. The backend
is Express + Socket.IO with rooms in a `Map`: one room per game with a
max-player limit. When a client joins, the server puts it in a Socket.IO
room, confirms with `joined_room`, and broadcasts `room_update` on every
join, leave, and disconnect — that's what drives the live player count.
Chat is a `send_message` emit broadcast as `receive_message`. Errors like a
full room come back as `room_error` and surface as a Snackbar. Disconnect
handling matters: an abrupt drop removes the player and re-broadcasts, so
counts stay honest. I verified the whole flow with a script that connects
three socket.io-client instances and asserts on the broadcasts — joins,
chat, leave, disconnect, and empty-room cleanup all pass.*

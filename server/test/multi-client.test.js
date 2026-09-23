/**
 * Phase 13: multi-client test.
 *
 * Simulates several Android clients joining rooms, chatting and leaving
 * using socket.io-client over the wire (no emulator needed).
 *
 * Run with the server up:  npm test   (from the server/ directory)
 * Exit code 0 = all assertions passed.
 */
const { io } = require("socket.io-client");

const URL = process.env.SERVER_URL || "http://localhost:3000";

/** Tiny assertion helpers so the test has no dependencies. */
let failures = 0;
function check(cond, label) {
  if (cond) {
    console.log(`  ok - ${label}`);
  } else {
    failures++;
    console.error(`  FAIL - ${label}`);
  }
}

/** Connect a fake client and wait for the socket to be open. */
function connect() {
  return new Promise((resolve) => {
    const socket = io(URL, { transports: ["websocket"] });
    socket.on("connect", () => resolve(socket));
  });
}

/** Wait for a specific event once. */
function once(socket, event) {
  return new Promise((resolve) => socket.once(event, resolve));
}

/** Wait for a room_update that reports N players in the given room. */
function waitForPlayerCount(socket, roomId, count, timeoutMs = 3000) {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(
      () => reject(new Error(`timeout waiting for ${count} players in ${roomId}`)),
      timeoutMs
    );
    function handler(update) {
      if (update.roomId === roomId && update.players.length === count) {
        clearTimeout(timer);
        socket.off("room_update", handler);
        resolve(update);
      }
    }
    socket.on("room_update", handler);
  });
}

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

async function main() {
  console.log(`Testing against ${URL}\n`);

  // --- 1. Three clients join the chess room ---
  console.log("1) join_room: three clients join chess");
  const alice = await connect();
  const bob = await connect();
  const carol = await connect();

  alice.emit("join_room", { gameId: "chess", playerName: "Alice" });
  const joinedAlice = await once(alice, "joined_room");
  check(joinedAlice.players.length === 1, "Alice sees 1 player after joining");
  check(joinedAlice.yourName === "Alice", "server confirms Alice's name");

  bob.emit("join_room", { gameId: "chess", playerName: "Bob" });
  const update2 = await waitForPlayerCount(alice, "chess-room", 2);
  check(update2.players.length === 2, "Alice's room_update shows 2 players");

  carol.emit("join_room", { gameId: "chess", playerName: "Carol" });
  await waitForPlayerCount(alice, "chess-room", 3);
  const update3 = await once(bob, "room_update");
  check(update3.players.length === 3, "Bob's room_update shows 3 players");

  // --- 2. Chat ---
  console.log("\n2) send_message / receive_message");
  alice.emit("send_message", { text: "gg everyone" });
  const msg = await once(carol, "receive_message");
  check(msg.sender === "Alice" && msg.text === "gg everyone", "Carol receives Alice's chat");

  bob.emit("send_message", { text: "" });
  const chatError = await once(bob, "room_error");
  check(chatError.code === "EMPTY_MESSAGE", "empty message rejected with room_error");

  // --- 3. Live player count on leave ---
  console.log("\n3) leave_room: player count drops");
  carol.emit("leave_room");
  await waitForPlayerCount(alice, "chess-room", 2);
  check(true, "room_update shows 2 players after Carol leaves");

  // --- 4. Disconnect handling ---
  console.log("\n4) disconnect: room shrinks when a client vanishes");
  bob.disconnect();
  await waitForPlayerCount(alice, "chess-room", 1);
  check(true, "room_update shows 1 player after Bob disconnects");

  // --- 5. Error cases ---
  console.log("\n5) error cases");
  alice.emit("join_room", { gameId: "nope", playerName: "Alice" });
  const badGame = await once(alice, "room_error");
  check(badGame.code === "UNKNOWN_GAME", "unknown game rejected");

  // --- 6. Room empties out and is deleted ---
  console.log("\n6) cleanup: empty room is removed");
  alice.emit("leave_room");
  const leftAck = await once(alice, "left_room");
  check(leftAck.roomId === "chess-room", "server confirms Alice left");
  await sleep(200);
  const res = await fetch(`${URL}/rooms`);
  const rooms = await res.json();
  check(Array.isArray(rooms) && !rooms.some((r) => r.roomId === "chess-room"), "chess room deleted when empty");

  alice.close();
  carol.close();

  console.log(failures === 0 ? "\nALL TESTS PASSED" : `\n${failures} TEST(S) FAILED`);
  process.exit(failures === 0 ? 0 : 1);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});

const socket = new WebSocket("wss://localhost:4000/wss");

socket.onmessage = function (event) {
  console.log(event.data);
}
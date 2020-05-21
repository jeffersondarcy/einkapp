const socket = new WebSocket(`ws://${window.location.host}`);
const imageElement = document.getElementById('mainScreenImg');

const getImageSrcFromData = async (event) => {
    if (!event.data || !(event.data instanceof Blob)) return null

    const reader = new FileReader();
    reader.readAsDataURL(event.data);
    return reader.result
}
socket.onmessage = async (event) => {
    if (!event.data || !(event.data instanceof Blob)) return
    console.log(event)

    const reader = new FileReader();
    reader.addEventListener('load', () => {
        document.getElementById('mainScreenImg').setAttribute('src', reader.result)
        console.log('bla');
    })
    reader.readAsDataURL(event.data);
}


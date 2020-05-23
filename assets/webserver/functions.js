const imageElement = document.getElementById('mainScreenImg');
const imageUrl = () => `${window.location.href}screenshot?${new Date().getTime()}`
//const imageUrl = () => 'http://192.168.0.164:3000/screenshot'

const reloadImage = () => {

    document.getElementById('mainScreenImg').setAttribute('src', imageUrl())

}

const evtSource = new EventSource(`${window.location.href}sse`);
evtSource.onmessage = console.log;
//evtSource.onmessage = reloadImage;


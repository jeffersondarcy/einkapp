const imageElement = document.getElementById('mainScreenImg');
const imageUrl = () => `${window.location.href}screenshot?${new Date().getTime()}`

const reloadImage = () => {
    console.log(new Date().getTime())
    imageElement.setAttribute('src', imageUrl())
}

const evtSource = new EventSource(`${window.location.href}sse`);
evtSource.onmessage = reloadImage;


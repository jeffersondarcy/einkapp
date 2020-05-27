var imageElement = document.getElementById('mainScreenImg');
var reader;

reloadImage();

if (!!window.FileReader) {
    reader = new FileReader();
    reader.addEventListener('load', function () {
        imageElement.setAttribute('src', reader.result)
    })
}

function imageUrl() {
    return window.location.href + 'screenshot?' + new Date().getTime()
}

function reloadImage() {
    imageElement.setAttribute('src', imageUrl())
}


if (!!window.WebSocket && !!window.FileReader) {
    var socket = new WebSocket('ws://' + window.location.host);
    socket.onmessage = function(event){
        if (!event.data || !(event.data instanceof Blob)) return null
        reader.readAsDataURL(event.data);
    }
}
else {
    setInterval(reloadImage, 1000);
}

/*
var imageElement = document.getElementById('mainScreenImg');
setInterval(reloadImage, 100);

function writeShit() {
    document.body.innerHTML += '<div>bla</div>'
}
function reloadImage() {
    imageElement.src = imageUrl();
}

function imageUrl() {
    return window.location.href + 'screenshot' + new Date().getTime() + '.png'
}

 */
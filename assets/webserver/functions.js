var imageElement = document.getElementById('mainScreenImg');

function imageUrl() {
    return window.location.href + 'screenshot?' + new Date().getTime()
}

function reloadImage() {
    //document.body.innerHTML += '<div>' + imageUrl() + '</div>'
    imageElement.setAttribute('src', imageUrl())
}

var evtSource = new EventSource(window.location.href +'sse');
evtSource.onmessage = reloadImage;


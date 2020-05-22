const imageElement = document.getElementById('mainScreenImg');
const imageUrl = () => `${window.location.href}screenshot?${new Date().getTime()}`
//const imageUrl = () => 'http://192.168.0.164:3000/screenshot'
const reloadImage = () => {

    document.getElementById('mainScreenImg').setAttribute('src', imageUrl())
}

const reloadText = () => {

    document.getElementById('text').innerHTML('src', imageUrl())
}
reloadImage();
//setInterval(() => {reloadImage();console.log(imageUrl())}, 2000);
setInterval(() => {document.write(Date())}, 2000);
//reloadImage();


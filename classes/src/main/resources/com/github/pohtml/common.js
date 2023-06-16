model = JSON.parse(model.textContent)
let date = new Date();
let timestamp = date.getTime()
sessionStorage.setItem(timestamp, JSON.stringify(model));
date.setTime(date.getTime() + 2000)
let view = model["com.github.pohtml.view"]
document.cookie = "pohtml_model=" + timestamp + ";expires=" + date.toUTCString() + ";path=" + view
location.replace(location.origin + view)
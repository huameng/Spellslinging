function readTextFile(file) {
  var xhr = new XMLHttpRequest();
  xhr.open("GET", file, true);

  xhr.onerror = function (e) {
    console.error(xhr.statusText);
  };
  xhr.send();
  return xhr;
}







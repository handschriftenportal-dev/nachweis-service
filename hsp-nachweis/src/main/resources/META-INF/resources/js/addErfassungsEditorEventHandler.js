document.addEventListener("BeschreibungBearbeitenEvent", (event) => {
  PrimeFaces.ab({
    s: "erfassungseditorform:sperrenRemoteCommand",
    f: "erfassungseditorform",
    u: "beschreibungdetailform",
  });
});

document.addEventListener("BeschreibungLesenEvent", (event) => {
  PrimeFaces.ab({
    s: "erfassungseditorform:entsperrenRemoteCommand",
    f: "erfassungseditorform",
    u: "beschreibungdetailform:headline",
  });
});

document.addEventListener("BeschreibungSpeichernEvent", (event) => {
  PrimeFaces.ab({
    s: "erfassungseditorform:updateSiteRemoteCommand",
    f: "erfassungseditorform",
    u: "beschreibungdetailform",
  });
});

document.addEventListener("BeschreibungAktualisierenEvent", (event) => {
  PrimeFaces.ab({
    s: "erfassungseditorform:checkSperrenCommand",
    f: "erfassungseditorform",
    u: "beschreibungdetailform:headline",
  });
});

function throttle(fn, delay_ms = 1000) {
  let timerFlag;
  return function throttled_fn(...args) {
    if (!timerFlag) {
      fn(...args);
      timerFlag = setTimeout(() => {
        timerFlag = undefined;
      }, delay_ms)
    }
  };
}

document.addEventListener("BeschreibungSessionAktualisierenEvent", throttle(() => {
  PrimeFaces.ab({
    s: "sessionform:sessionAktualisierenRemoteCommand",
    f: "sessionform",
  });
}, 10000));


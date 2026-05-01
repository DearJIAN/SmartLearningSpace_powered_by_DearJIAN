/*!
 * Live2D Widget
 * https://github.com/stevenjoezhang/live2d-widget
 */

const live2d_path = '/live2d-widget-dist/';
const live2d_version = '?v=20260420d';

function loadExternalResource(url, type) {
  return new Promise((resolve, reject) => {
    let tag;

    if (type === 'css') {
      tag = document.createElement('link');
      tag.rel = 'stylesheet';
      tag.href = url + live2d_version;
    }
    else if (type === 'js') {
      tag = document.createElement('script');
      tag.type = 'module';
      tag.src = url + live2d_version;
    }
    if (tag) {
      tag.onload = () => resolve(url);
      tag.onerror = () => reject(url);
      document.head.appendChild(tag);
    }
  });
}

(async () => {
  if (window.waifuLoaded) {
    console.log("[Live2D] Waifu already loaded, skipping...");
    return;
  }
  window.waifuLoaded = true;

  if (screen.width < 768) return;

  const OriginalImage = window.Image;
  window.Image = function(...args) {
    const img = new OriginalImage(...args);
    img.crossOrigin = "anonymous";
    return img;
  };
  window.Image.prototype = OriginalImage.prototype;

  await Promise.all([
    loadExternalResource(live2d_path + 'waifu.css', 'css'),
    loadExternalResource(live2d_path + 'waifu-tips.js', 'js')
  ]);

  console.log("[Live2D] Resources loaded, waiting for app init...");
})();

console.log(`\n%cLive2D%cWidget%c\n`, 'padding: 8px; background: #cd3e45; font-weight: bold; font-size: large; color: white;', 'padding: 8px; background: #ff5450; font-size: large; color: #eee;', '');
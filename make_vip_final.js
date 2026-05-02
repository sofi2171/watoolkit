const fs = require('fs');

// A. index.html mein link change karein
const indexPath = './www/index.html';
if(fs.existsSync(indexPath)) {
    let indexHtml = fs.readFileSync(indexPath, 'utf8');
    // Purane link ko naye link se badal dein
    indexHtml = indexHtml.replace(/status-saver\.html/g, 'vip-status.html');
    // Service worker ki registration khatam karein
    indexHtml = indexHtml.replace(/navigator\.serviceWorker\.register/g, 'console.log("Cache Killed"); //');
    fs.writeFileSync(indexPath, indexHtml);
    console.log('✅ index.html linked to new VIP page!');
}

// B. Nayi vip-status.html file banayein
const vipHtml = `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>VIP Status Saver</title>
    <style>
        body { font-family: sans-serif; margin: 0; background: #f0f2f5; }
        header { background: #075e54; color: white; padding: 15px; text-align: center; font-size: 20px; font-weight: bold; position: sticky; top: 0; z-index: 100;}
        .back-btn { float: left; cursor: pointer; font-size: 22px; margin-top: -2px; }
        .tabs { display: flex; background: #128c7e; }
        .tab { flex: 1; text-align: center; padding: 12px; color: white; opacity: 0.7; font-weight: bold; transition: 0.3s; cursor: pointer; }
        .tab.active { opacity: 1; border-bottom: 3px solid white; }
        .container { padding: 10px; display: flex; flex-wrap: wrap; gap: 10px; }
        .card { width: calc(50% - 5px); background: #000; border-radius: 8px; overflow: hidden; position: relative; height: 160px; box-shadow: 0 2px 4px rgba(0,0,0,0.2); }
        .media-box { width: 100%; height: 100%; cursor: pointer; }
        .card img, .card video { width: 100%; height: 100%; object-fit: cover; }
        .download-btn { position: absolute; bottom: 8px; right: 8px; background: #25d366; color: white; border: none; border-radius: 50%; width: 40px; height: 40px; font-size: 20px; font-weight: bold; box-shadow: 0 2px 5px rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 10; cursor: pointer;}
        .play-icon { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: white; font-size: 45px; text-shadow: 0 2px 5px rgba(0,0,0,0.6); pointer-events: none;}
        #permission-box { text-align: center; padding: 40px 20px; display: none; }
        .btn { background: #128c7e; color: white; padding: 12px 24px; border: none; border-radius: 5px; font-size: 16px; margin-top: 15px; font-weight: bold; cursor: pointer; }
    </style>
</head>
<body>
    <header>
        <span class="back-btn" onclick="window.history.back()">←</span> 
        VIP Status Saver
    </header>
    <div class="tabs">
        <div class="tab active" onclick="switchTab('images')">Images</div>
        <div class="tab" onclick="switchTab('videos')">Videos</div>
    </div>
    <div id="permission-box">
        <h2>Permission Required ⚠️</h2>
        <p>App needs Storage access to view statuses.</p>
        <button class="btn" onclick="askPermission()">Allow Permission</button>
    </div>
    <div class="container" id="media-container"></div>
    <script>
        let allStatuses = [];
        let currentTab = 'images';
        window.addEventListener('load', () => { setTimeout(checkAndLoad, 500); });
        async function askPermission() {
            try {
                const fs = window.Capacitor.Plugins.Filesystem;
                await fs.requestPermissions(); 
                setTimeout(checkAndLoad, 1000);
            } catch(e) {
                alert('Please allow Storage permission from settings.');
            }
        }
        async function checkAndLoad() {
            const container = document.getElementById('media-container');
            const permBox = document.getElementById('permission-box');
            try {
                const fs = window.Capacitor.Plugins.Filesystem;
                const result = await fs.readdir({
                    path: 'Android/media/com.whatsapp/WhatsApp/Media/.Statuses',
                    directory: 'EXTERNAL_STORAGE'
                });
                permBox.style.display = 'none';
                let uniqueMap = new Map();
                result.files.forEach(f => {
                    if(f.name.endsWith('.jpg') || f.name.endsWith('.mp4')) {
                        uniqueMap.set(f.name, f);
                    }
                });
                allStatuses = Array.from(uniqueMap.values());
                if(allStatuses.length === 0) {
                    container.innerHTML = '<h3 style="text-align:center; width:100%; color:#555;">No statuses found!</h3>';
                } else {
                    renderMedia();
                }
            } catch(e) {
                container.innerHTML = '';
                permBox.style.display = 'block';
            }
        }
        function switchTab(tab) {
            currentTab = tab;
            document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
            event.target.classList.add('active');
            renderMedia();
        }
        function renderMedia() {
            const container = document.getElementById('media-container');
            container.innerHTML = '';
            const filtered = allStatuses.filter(f => currentTab === 'images' ? f.name.endsWith('.jpg') : f.name.endsWith('.mp4'));
            filtered.forEach(file => {
                const src = window.Capacitor.convertFileSrc(file.uri);
                const card = document.createElement('div');
                card.className = 'card';
                const mediaBox = document.createElement('div');
                mediaBox.className = 'media-box';
                if(currentTab === 'images') {
                    mediaBox.innerHTML = \`<img src="\${src}">\`;
                } else {
                    mediaBox.innerHTML = \`<video src="\${src}" preload="auto" playsinline muted></video><div class="play-icon">▶</div>\`;
                }
                mediaBox.onclick = () => viewMedia(src, currentTab === 'images' ? 'image' : 'video');
                card.appendChild(mediaBox);
                const btn = document.createElement('button');
                btn.className = 'download-btn';
                btn.innerHTML = '⬇';
                btn.onclick = (e) => { e.stopPropagation(); downloadMedia(file.uri, file.name); };
                card.appendChild(btn);
                container.appendChild(card);
            });
        }
        async function downloadMedia(uri, name) {
            try {
                const fs = window.Capacitor.Plugins.Filesystem;
                await fs.copy({ from: uri, to: 'Download/' + name, toDirectory: 'EXTERNAL_STORAGE' });
                alert('✅ Saved successfully in Downloads folder!');
            } catch(e) {
                alert('⚠️ Download Failed: ' + e.message);
            }
        }
        function viewMedia(src, type) {
            const viewer = document.createElement('div');
            viewer.style.cssText = 'position:fixed; top:0; left:0; width:100%; height:100%; background:black; z-index:99999; display:flex; justify-content:center; align-items:center;';
            const closeBtn = document.createElement('div');
            closeBtn.innerText = '✖';
            closeBtn.style.cssText = 'position:absolute; top:20px; right:20px; color:white; font-size:35px; z-index:100000; background: rgba(0,0,0,0.5); border-radius:50%; width: 50px; height: 50px; display:flex; justify-content:center; align-items:center; cursor:pointer;';
            closeBtn.onclick = () => document.body.removeChild(viewer);
            viewer.appendChild(closeBtn);
            if(type === 'image') {
                viewer.innerHTML += \`<img src="\${src}" style="max-width:100%; max-height:100%; object-fit:contain;">\`;
            } else {
                viewer.innerHTML += \`<video src="\${src}" controls autoplay style="max-width:100%; max-height:100%; object-fit:contain; outline:none;"></video>\`;
            }
            document.body.appendChild(viewer);
        }
    </script>
</body>
</html>`;
fs.writeFileSync('./www/vip-status.html', vipHtml);
console.log('✅ Brand new vip-status.html file created!');

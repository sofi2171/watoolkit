const fs = require('fs');

// 1. Manifest mein Permission add karna
const manifestPath = './android/app/src/main/AndroidManifest.xml';
if (fs.existsSync(manifestPath)) {
    let manifest = fs.readFileSync(manifestPath, 'utf8');
    if (!manifest.includes('MANAGE_EXTERNAL_STORAGE')) {
        const permissions = `
    <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />`;
        manifest = manifest.replace('</manifest>', permissions + '\n</manifest>');
        fs.writeFileSync(manifestPath, manifest);
        console.log('✅ Permissions Added to AndroidManifest.xml!');
    }
}

// 2. Status Saver ka mukammal HTML/JS Code
const htmlCode = `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>Status Saver VIP</title>
    <style>
        body { font-family: sans-serif; margin: 0; background: #f0f2f5; }
        header { background: #075e54; color: white; padding: 15px; text-align: center; font-size: 20px; font-weight: bold; position: sticky; top: 0; z-index: 100;}
        .tabs { display: flex; background: #128c7e; }
        .tab { flex: 1; text-align: center; padding: 12px; color: white; opacity: 0.7; font-weight: bold; transition: 0.3s; }
        .tab.active { opacity: 1; border-bottom: 3px solid white; }
        .container { padding: 10px; display: flex; flex-wrap: wrap; gap: 10px; }
        .card { width: calc(50% - 5px); background: #000; border-radius: 8px; overflow: hidden; position: relative; height: 160px; box-shadow: 0 2px 4px rgba(0,0,0,0.2); }
        .card img, .card video { width: 100%; height: 100%; object-fit: cover; }
        .download-btn { position: absolute; bottom: 8px; right: 8px; background: #25d366; color: white; border: none; border-radius: 50%; width: 40px; height: 40px; font-size: 20px; font-weight: bold; box-shadow: 0 2px 5px rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; }
        .play-icon { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: white; font-size: 45px; text-shadow: 0 2px 5px rgba(0,0,0,0.6); pointer-events: none;}
        #permission-box { text-align: center; padding: 40px 20px; display: none; }
        .btn { background: #128c7e; color: white; padding: 12px 24px; border: none; border-radius: 5px; font-size: 16px; margin-top: 15px; font-weight: bold; }
    </style>
</head>
<body>
    <header>Status Saver</header>
    <div class="tabs">
        <div class="tab active" onclick="switchTab('images')">Images</div>
        <div class="tab" onclick="switchTab('videos')">Videos</div>
    </div>

    <div id="permission-box">
        <h2>Permission Required ⚠️</h2>
        <p>App needs <b>All Files Access</b> to read WhatsApp statuses.</p>
        <p style="font-size: 13px; color: #555;">Please go to Mobile Settings > Apps > Permissions > Allow All Files Access, then click below.</p>
        <button class="btn" onclick="loadStatuses()">Reload Statuses</button>
    </div>

    <div class="container" id="media-container"></div>

    <script>
        let allStatuses = [];
        let currentTab = 'images';

        window.addEventListener('load', () => { setTimeout(loadStatuses, 500); });

        async function loadStatuses() {
            const container = document.getElementById('media-container');
            const permBox = document.getElementById('permission-box');
            container.innerHTML = '<h3 style="text-align:center; width:100%; color:#555;">Loading Statuses...</h3>';
            permBox.style.display = 'none';

            try {
                const Filesystem = window.Capacitor.Plugins.Filesystem;
                const result = await Filesystem.readdir({
                    path: 'Android/media/com.whatsapp/WhatsApp/Media/.Statuses',
                    directory: 'EXTERNAL_STORAGE'
                });

                allStatuses = result.files.filter(f => f.name.endsWith('.jpg') || f.name.endsWith('.mp4'));
                
                if(allStatuses.length === 0) {
                    container.innerHTML = '<h3 style="text-align:center; width:100%; color:#555;">No statuses found. Open WhatsApp to view some first!</h3>';
                } else {
                    renderMedia();
                }
            } catch(e) {
                console.error('Permission error: ', e);
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
                
                if(currentTab === 'images') {
                    card.innerHTML = \`<img src="\${src}" onclick="viewMedia('\${src}', 'image')">\`;
                } else {
                    card.innerHTML = \`<video src="\${src}#t=0.1" preload="metadata" onclick="viewMedia('\${src}', 'video')"></video><div class="play-icon">▶</div>\`;
                }

                card.innerHTML += \`<button class="download-btn" onclick="downloadMedia('\${file.uri}', '\${file.name}')">⬇</button>\`;
                container.appendChild(card);
            });
        }

        async function downloadMedia(uri, name) {
            try {
                const Filesystem = window.Capacitor.Plugins.Filesystem;
                await Filesystem.copy({
                    from: uri,
                    to: 'Download/' + name,
                    toDirectory: 'EXTERNAL_STORAGE'
                });
                alert('✅ Status Downloaded in Gallery/Downloads folder!');
            } catch(e) {
                alert('⚠️ Download Failed: ' + e.message);
            }
        }

        function viewMedia(src, type) {
            const viewer = document.createElement('div');
            viewer.style.cssText = 'position:fixed; top:0; left:0; width:100%; height:100%; background:black; z-index:99999; display:flex; justify-content:center; align-items:center;';
            
            const closeBtn = document.createElement('div');
            closeBtn.innerText = '✖';
            closeBtn.style.cssText = 'position:absolute; top:20px; right:20px; color:white; font-size:35px; z-index:100000; background: rgba(0,0,0,0.5); border-radius:50%; width: 50px; height: 50px; display:flex; justify-content:center; align-items:center;';
            closeBtn.onclick = () => document.body.removeChild(viewer);
            viewer.appendChild(closeBtn);

            if(type === 'image') {
                viewer.innerHTML += \`<img src="\${src}" style="max-width:100%; max-height:100%; object-fit:contain;">\`;
            } else {
                viewer.innerHTML += \`<video src="\${src}" controls autoplay style="max-width:100%; max-height:100%; object-fit:contain;"></video>\`;
            }
            document.body.appendChild(viewer);
        }
    </script>
</body>
</html>`;

fs.writeFileSync('./www/status-saver.html', htmlCode);
console.log('✅ status-saver.html successfully generated!');

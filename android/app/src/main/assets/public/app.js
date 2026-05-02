let folderUri = "";

async function pickFolder() {
  await Capacitor.Plugins.AppNativePlugin.pickFolder();
  const res = await Capacitor.Plugins.AppNativePlugin.getFolder();
  folderUri = res.uri;
  alert("Folder Selected!");
}

async function openNotificationSettings() {
  await Capacitor.Plugins.AppNativePlugin.openNotificationSettings();
}

function showGuide() {
  alert("Select: Android > media > com.whatsapp > WhatsApp > Media > .Statuses");
}


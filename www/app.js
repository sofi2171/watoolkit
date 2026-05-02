async function pickFolder() {
  await Capacitor.Plugins.AppNativePlugin.pickStatusFolder();
}

async function getFolder() {
  const res = await Capacitor.Plugins.AppNativePlugin.getPickedFolder();
  console.log("Selected Folder URI:", res.uri);
}

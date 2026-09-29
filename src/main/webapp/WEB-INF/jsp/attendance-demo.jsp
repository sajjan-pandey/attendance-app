<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
  <title>Attendance Face POC</title>
  <script src="https://cdn.jsdelivr.net/npm/face-api.js@0.22.2/dist/face-api.min.js"></script>
  <style>
    :root{--primary:#2563eb;--primary-dark:#1d4ed8;--ink:#172033;--muted:#64748b;--line:#e5eaf2;--surface:#fff;--soft:#f8fafc}
    *{box-sizing:border-box}
    html{background:#eef4ff}
    body{font-family:Inter,ui-sans-serif,system-ui,-apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif;max-width:1160px;margin:0 auto;padding:0 18px 32px;background:linear-gradient(135deg,#eef4ff,#f8fafc);color:var(--ink);min-height:100vh}
    .app-header{display:flex;align-items:center;justify-content:space-between;gap:16px;padding:28px 4px 20px}
    .brand{display:flex;align-items:center;gap:12px}.brand-mark{display:grid;place-items:center;width:44px;height:44px;border-radius:14px;background:linear-gradient(135deg,var(--primary),#60a5fa);color:#fff;font-size:23px;font-weight:800;box-shadow:0 8px 18px #2563eb38}.brand-copy h1{margin:0;font-size:clamp(24px,4vw,32px);letter-spacing:-.03em;color:#12316b}.subtitle{color:var(--muted);margin:5px 0 0;font-size:14px}.status-pill{padding:8px 12px;border-radius:999px;background:#dcfce7;color:#166534;font-size:12px;font-weight:800;white-space:nowrap}.app-main{display:grid;grid-template-columns:minmax(0,1.25fr) minmax(300px,.75fr);gap:18px;align-items:start}.card{background:rgba(255,255,255,.94);border:1px solid var(--line);border-radius:18px;padding:22px;margin:0 0 18px;box-shadow:0 10px 30px #18264d0d}.card h2{display:flex;align-items:center;gap:8px;margin:0 0 14px;font-size:19px;letter-spacing:-.01em}.card p{line-height:1.55}.card--wide{grid-column:1/-1}.section-number{display:inline-grid;place-items:center;width:25px;height:25px;border-radius:8px;background:#dbeafe;color:var(--primary-dark);font-size:12px}.hint{font-size:13px;color:var(--muted)}label{display:block;margin:10px 0 6px;font-size:13px;font-weight:750;color:#334155}select,input{padding:12px;width:100%;min-height:44px;border:1px solid #cbd5e1;border-radius:10px;background:#fff;font-size:14px;color:var(--ink);outline:none;transition:border-color .2s,box-shadow .2s}select:focus,input:focus{border-color:#60a5fa;box-shadow:0 0 0 3px #dbeafe}.grid{display:grid;grid-template-columns:1fr 1fr;gap:14px}.full{grid-column:1/-1}button{background:var(--primary);color:#fff;border:0;min-height:44px;padding:12px 18px;border-radius:10px;cursor:pointer;margin-top:15px;font-weight:750;transition:.2s;box-shadow:0 4px 10px #2563eb22}button:hover{background:var(--primary-dark);transform:translateY(-1px)}button:disabled{background:#94a3b8;cursor:not-allowed;transform:none;box-shadow:none}.secondary{background:#475569}.secondary:hover{background:#334155}.camera-wrap{position:relative;width:min(100%,520px);aspect-ratio:4/3;margin-top:8px;background:#0f172a;border-radius:14px;overflow:hidden;box-shadow:0 8px 20px #0f172a26}.camera-video{display:block;width:100%;height:100%;object-fit:cover;background:#0f172a;border-radius:14px}.camera.mirrored{transform:scaleX(-1)}.camera-wrap:not(.camera-ready) .face-guide,.camera-wrap:not(.camera-ready) .pose-instruction{display:none}.camera{display:block;width:100%;background:#0f172a;border-radius:14px}.face-snapshot{width:224px;max-width:100%;aspect-ratio:1/1;object-fit:contain;margin-top:10px}.face-guide{position:absolute;left:50%;top:48%;width:58%;aspect-ratio:1/1.25;transform:translate(-50%,-50%);border:4px solid #facc15;border-radius:50%;box-shadow:0 0 0 999px #0f172a8c;pointer-events:none}.pose-instruction{position:absolute;left:0;right:0;bottom:12px;text-align:center;color:#fff;font-weight:750;text-shadow:0 1px 3px #000;font-size:15px;pointer-events:none;padding:0 12px}.validation-grid{display:grid;grid-template-columns:repeat(2,minmax(0,220px));gap:7px;margin-top:10px;max-width:460px}.check{padding:9px 10px;border-radius:8px;background:#f1f5f9;color:var(--muted);font-size:12px;font-weight:750}.check.pass{background:#dcfce7;color:#166534}.check.fail{background:#fee2e2;color:#991b1b}.check.pending{background:#fef3c7;color:#92400e}.ok,.bad{padding:13px;border-radius:10px}.ok{background:#dcfce7;color:#166534}.bad{background:#fee2e2;color:#991b1b}table{width:100%;border-collapse:collapse}th,td{text-align:left;padding:11px;border-bottom:1px solid #e5e7eb;font-size:13px}th{background:#f8fafc;color:#475569;font-size:12px;text-transform:uppercase;letter-spacing:.03em}.table-wrap{overflow-x:auto}code{background:#eef2ff;padding:3px 6px;border-radius:5px;overflow-wrap:anywhere}
    @media(max-width:860px){.app-main{display:block}.card--wide{grid-column:auto}}
    @media(max-width:600px){body{padding:0 12px 24px}.app-header{padding:18px 2px 16px;align-items:flex-start}.brand-mark{width:40px;height:40px;border-radius:12px}.brand-copy h1{font-size:24px}.subtitle{font-size:12px;max-width:250px}.status-pill{font-size:11px;padding:7px 9px}.card{padding:16px;margin-bottom:12px;border-radius:15px}.card h2{font-size:17px}.grid{grid-template-columns:1fr;gap:8px}.full{grid-column:auto}button{width:100%;margin-top:12px}.camera-wrap{width:100%;border-radius:12px}.validation-grid{grid-template-columns:1fr 1fr}.table-wrap{margin:0 -4px;overflow:visible}table,thead,tbody,tr,th,td{display:block}table{min-width:0}thead{display:none}tr{padding:12px 0;border-bottom:1px solid #e5e7eb}tr:last-child{border-bottom:0}td{display:flex;justify-content:space-between;gap:12px;padding:5px 0;border:0;text-align:right;font-size:12px}td:before{content:attr(data-label);font-weight:750;color:#64748b;text-align:left}td:first-child{font-size:14px;font-weight:750;color:#172033}td:first-child:before{content:"User"}}
  </style>
</head>
<body>
  <header class="app-header">
    <div class="brand"><div class="brand-mark">✓</div><div class="brand-copy"><h1>Face Attendance</h1><p class="subtitle">Secure, location-based attendance</p></div></div>
    <span class="status-pill">● System ready</span>
  </header>
  <main class="app-main">

  <div class="card">
    <h2><span class="section-number">01</span>Face enrollment <span class="hint">(one-time setup)</span></h2>
    <p class="hint">Selected enrollment user: <strong id="enrollmentUserLabel">Loading...</strong></p>
    <p class="hint">Selected user ka clear face image upload karein. Is image ka raw photo save nahi hoga; sirf ArcFace embedding database mein save hogi.</p>
    <label for="enrollmentImage">Enrollment image</label>
    <input id="enrollmentImage" type="file" accept="image/jpeg,image/png,image/webp">
    <button id="enrollImageButton" type="button" onclick="enrollUploadedFace()">Enroll uploaded face</button>
    <button id="liveEnrollButton" type="button" class="secondary" onclick="startLiveEnrollment()" disabled>Enroll using live camera</button>
    <p id="enrollmentStatus" class="hint"></p>
  </div>

  <div class="card">
    <h2><span class="section-number">02</span>Mark attendance</h2>
    <form method="post" action="${pageContext.request.contextPath}/demo/attendance/submit" onsubmit="event.preventDefault(); return false;">
      <div class="grid">
        <div><label>User</label><select id="userCode" name="userCode" onchange="handleUserChange()" required><c:forEach var="u" items="${users}"><option value="${u.code}" data-assigned-location="${u.assignedLocationCode}" ${u.code == 'EMP004' ? 'selected' : ''}>${u.code} - ${u.name} (assigned: ${u.assignedLocationCode})</option></c:forEach></select></div>
        <div><label>Location / Camp</label><select id="locationCode" name="locationCode" required><c:forEach var="location" items="${locations}"><option value="${location.code}" ${location.code == 'CAMP004' ? 'selected' : ''}>${location.code} - ${location.name}</option></c:forEach></select></div>
        <div><label>Punch type</label><select name="attendanceType" required><option value="IN">IN - Punch in</option><option value="OUT">OUT - Punch out</option></select></div>
        <div><label>Current latitude</label><input id="latitude" name="latitude" placeholder="Waiting for GPS..." type="number" step="any" required></div>
        <div><label>Current longitude</label><input id="longitude" name="longitude" placeholder="Waiting for GPS..." type="number" step="any" required></div>
        <div class="full"><span id="gpsStatus" class="hint">GPS location is being detected...</span></div>
        <div class="full"><label>Live face verification</label><div id="cameraWrap" class="camera-wrap"><video id="camera" class="camera camera-video" autoplay playsinline></video><div id="faceGuide" class="face-guide"></div><div id="poseInstruction" class="pose-instruction">Face circle ke andar rakhein</div></div><button type="button" class="secondary" onclick="flipCamera()">Flip camera</button><div class="validation-grid"><div id="checkFaceCount" class="check">Face: waiting</div><div id="checkPosition" class="check">Position: waiting</div><div id="checkQuality" class="check">Quality: waiting</div><div id="checkPose" class="check">Pose: waiting</div><div id="checkStable" class="check">Stable 1s: waiting</div><div id="checkLiveness" class="check">Liveness: pending</div></div><p id="captureStatus" class="hint">Auto capture: face ka wait ho raha hai...</p><canvas id="snapshot" class="camera face-snapshot" style="display:none"></canvas><input id="faceImageBase64" name="faceImageBase64" type="hidden"><input id="faceSourceImageBase64" name="sourceImageBase64" type="hidden"><input id="livenessSessionId" name="livenessSessionId" type="hidden"><button id="liveAttendanceButton" type="button" onclick="startLiveAttendance()">Start live capture</button><p id="cameraStatus" class="hint">Face model loading...</p></div>
      </div>
      <p class="hint">Camera face detect karega, automatically match karega aur attendance submit karega.</p>
    </form>
    <c:if test="${not empty result}"><p class="${result.success ? 'ok' : 'bad'}"><c:out value="${result.message}"/></p><p class="hint">Result yahin rahega. Naya attempt ya manual refresh karne par hi page badlega.</p></c:if>
  </div>

  <div class="card"><h2><span class="section-number">03</span>Demo users</h2><table><tr><th>User</th><th>Assigned location</th></tr><c:forEach var="u" items="${users}"><tr><td data-label="User">${u.code} - ${u.name}</td><td data-label="Assigned location">${u.assignedLocationCode}</td></tr></c:forEach></table></div>
  <c:if test="${not empty result and result.success and not empty result.attendance}">
    <div class="card"><h2><span class="section-number">04</span>Latest punch response</h2>
      <p><strong>${result.attendance.attendanceType}</strong> punch for ${result.attendance.userCode} at ${result.attendance.recordedAt}</p>
      <p>Punch GPS: <code>${result.attendance.latitude}, ${result.attendance.longitude}</code></p>
      <p>Camp GPS: <code>${result.attendance.locationLatitude}, ${result.attendance.locationLongitude}</code></p>
      <p>Calculated distance: <strong>${result.attendance.distanceMeters} meters</strong> | Allowed radius: <strong>${result.attendance.allowedRadiusMeters} meters</strong></p>
    </div>
  </c:if>
  <div class="card card--wide"><h2><span class="section-number">05</span>Today's punches</h2><div class="table-wrap"><table><tr><th>User</th><th>Type</th><th>Location</th><th>Punch latitude</th><th>Punch longitude</th><th>Distance</th><th>Allowed radius</th><th>Time</th></tr><c:forEach var="a" items="${records}"><tr><td data-label="User">${a.userCode} - ${a.userName}</td><td data-label="Type"><strong>${a.attendanceType}</strong></td><td data-label="Location">${a.locationCode}</td><td data-label="Punch latitude">${a.latitude}</td><td data-label="Punch longitude">${a.longitude}</td><td data-label="Distance">${a.distanceMeters} m</td><td data-label="Allowed radius">${a.allowedRadiusMeters} m</td><td data-label="Time">${a.recordedAt}</td></tr></c:forEach></table></div></div>
  </main>
<script>
let cameraStream, cameraStartInProgress=false, previewCaptureTimer=null, previewFrameBusy=false, autoLiveKickoffStarted=false, cameraFacingMode='user', faceModelsReady=false, faceModelsLoadPromise=null, liveStartInProgress=false, backendAvailable=false, backendHealthTimer=null, clientFaceDetected=false, livenessFinishStarted=false, centerFaceImageBase64=null, autoCaptureTimer=null, serverFrameTimer=null, serverFrameBusy=false, serverFrameSequence=0, serverLivenessPassed=false, poseStage=0, stableSince=0, centerYaw=0, centerPitch=0, centerFaceX=0, centerFaceY=0, poseChallenges=[], completedChallenges=[], livenessSessionId=null, livenessNonce=null, livenessUserCode=null, livenessStartedAt=0, livenessAttemptActive=false, frameBusy=false, submitting=false, currentEnrollmentReady=false, enrollmentMode=false, lastFaceCenter=null, blinkOpenSeen=false, blinkClosed=false, blinkVerified=false, blinkOpenBaseline=0, lastBlinkEar=0, blinkOpenFrames=0, blinkClosedFrames=0, blinkReopenFrames=0, blinkLogCounter=0;
const faceModelUrls=[
  'https://justadudewhohacks.github.io/face-api.js/models',
  'https://cdn.jsdelivr.net/gh/justadudewhohacks/face-api.js@master/weights'
];
// Blink 100-300ms ka ho sakta hai; chhota input aur frequent sampling uske
// frames miss hone se bachate hain. Final capture ab bhi 320px par hota hai.
// Mobile/webcam lighting mein face score fluctuate karta hai; moderate
// threshold rakhein, final quality/liveness checks false positives rokengi.
const detectorOptions=new faceapi.TinyFaceDetectorOptions({inputSize:320, scoreThreshold:0.22});
const captureDetectorOptions=new faceapi.TinyFaceDetectorOptions({inputSize:320, scoreThreshold:0.25});
const FRONT_STABLE_MILLIS=500;
const LOCAL_FACE_CHECK_INTERVAL_MS=150;
const SERVER_FACE_CHECK_INTERVAL_MS=300;
function setCheck(id,state,text){
  const element=document.getElementById(id);
  element.className='check '+state;
  element.textContent=text;
}
function resetCaptureChecks(){
  setCheck('checkFaceCount','pending','Face: checking');
  setCheck('checkPosition','pending','Position: checking');
  setCheck('checkQuality','pending','Quality: checking');
  setCheck('checkPose','pending','Pose: checking');
  setCheck('checkStable','pending','Stable '+(FRONT_STABLE_MILLIS/1000)+'s: 0%');
  setCheck('checkLiveness','pending','Liveness: pending');
}
function resetStableCheck(){
  stableSince=0;
  // Front capture ke baad stability dobara wait nahi karni hai.
  setCheck('checkStable',poseStage===0 ? 'pending' : 'pass',poseStage===0 ? 'Stable '+(FRONT_STABLE_MILLIS/1000)+'s: 0%' : 'Stable: ready');
}
function challengeInstruction(challenge){
  return 'Head '+challenge+' karein';
}
async function loadFaceModels(){
  if(faceModelsReady) return true;
  if(faceModelsLoadPromise) return faceModelsLoadPromise;
  faceModelsLoadPromise=loadFaceModelsOnce();
  try { return await faceModelsLoadPromise; }
  finally { faceModelsLoadPromise=null; }
}
async function loadFaceModelsOnce(){
  const status=document.getElementById('cameraStatus');
  try{
    if(!window.faceapi) throw new Error('face-api.js library load nahi hui. CDN/network check karein.');
    status.textContent='Face detector models load ho rahe hain...';
    const timeout=new Promise((resolve,reject)=>setTimeout(()=>reject(new Error('Face model download timeout. Internet/model URL check karein.')),20000));
    let lastModelError;
    for(const modelUrl of faceModelUrls){
      try {
        status.textContent='Face models load ho rahe hain: '+modelUrl;
        const modelLoad=Promise.all([
          faceapi.nets.tinyFaceDetector.loadFromUri(modelUrl),
          faceapi.nets.faceLandmark68Net.loadFromUri(modelUrl)
        ]);
        await Promise.race([modelLoad,timeout]);
        if(faceapi.nets.tinyFaceDetector.isLoaded && faceapi.nets.faceLandmark68Net.isLoaded) break;
      } catch(error) {
        lastModelError=error;
      }
    }
    if(!faceapi.nets.tinyFaceDetector.isLoaded || !faceapi.nets.faceLandmark68Net.isLoaded)
      throw new Error('Face detector/landmark model load nahi hua: '+(lastModelError ? lastModelError.message : 'all model URLs failed'));
    faceModelsReady=true;
    console.info('Face models ready', {uiVersion:'attendance-ui-2026-09-29-1505'});
    document.getElementById('liveEnrollButton').disabled=false;
    // Models ready hote hi camera ko initialize kar do, taaki user ko
    // attendance/enrollment button dabane ke baad blank capture state na mile.
    await startCamera();
    if(!cameraStream) throw new Error('Camera start nahi hua. HTTPS aur camera permission check karein.');
    await refreshEnrollmentStatus();
    status.textContent='Face models ready. Camera live checks ke liye ready hai.';
    if(!autoLiveKickoffStarted){
      autoLiveKickoffStarted=true;
      setTimeout(()=>startLiveAttendance().catch(error=>{
        console.error('Automatic live capture start failed',error);
        status.textContent='Live capture start failed: '+error.message;
        document.getElementById('liveAttendanceButton').disabled=false;
      }),500);
    }
  }catch(error){
    faceModelsReady=false;
    document.getElementById('liveEnrollButton').disabled=true;
    document.getElementById('liveAttendanceButton').disabled=true;
    status.textContent='Face model load failed: '+error.message;
    console.error('Face model load failed',error);
  }
}
async function startCamera(){
  const status=document.getElementById('cameraStatus');
  if(cameraStartInProgress){
    while(cameraStartInProgress) await new Promise(resolve=>setTimeout(resolve,50));
    return;
  }
  if(!window.isSecureContext){
    status.textContent='Camera ke liye HTTPS required hai. http://localhost par desktop test ya HTTPS URL use karein.';
    return;
  }
  if(!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia){
    status.textContent='Is browser/device mein camera API available nahi hai. Chrome/Safari latest use karein.';
    return;
  }
  cameraStartInProgress=true;
  try{
    if(cameraStream && cameraStream.getVideoTracks().some(track=>track.readyState==='live')){
      const existingCamera=document.getElementById('camera');
      if(existingCamera.videoWidth && existingCamera.videoHeight){
        document.getElementById('cameraWrap').classList.add('camera-ready');
        status.textContent='Camera ready. Face ko yellow circle ke andar rakhein.';
        startAutoPreviewCapture();
        return;
      }
    }
    cameraStream=await navigator.mediaDevices.getUserMedia({video:{facingMode:{ideal:cameraFacingMode},width:{ideal:1280},height:{ideal:720},frameRate:{ideal:24,max:30}},audio:false});
    const videoTrack=cameraStream.getVideoTracks()[0];
    const capabilities=videoTrack && videoTrack.getCapabilities ? videoTrack.getCapabilities() : {};
    if(capabilities.focusMode && capabilities.focusMode.includes('continuous')){
      try { await videoTrack.applyConstraints({advanced:[{focusMode:'continuous'}]}); }
      catch(focusError) { /* Device autofocus constraint optional hai. */ }
    }
    const camera=document.getElementById('camera');
    camera.srcObject=cameraStream;
    camera.classList.toggle('mirrored',cameraFacingMode==='user');
    // Stream attach hone ke baad dimensions available hone tak capture/detection
    // start nahi hona chahiye. Kuch browsers mein srcObject set karne ke baad
    // videoWidth ek-do frames ke liye 0 rehta hai.
    if(camera.readyState < HTMLMediaElement.HAVE_METADATA){
      await new Promise((resolve,reject)=>{
        const timeout=setTimeout(()=>reject(new Error('Camera video frame ready nahi hua.')),5000);
        camera.addEventListener('loadedmetadata',()=>{ clearTimeout(timeout); resolve(); },{once:true});
      });
    }
    await camera.play();
    document.getElementById('cameraWrap').classList.add('camera-ready');
    status.textContent=(cameraFacingMode==='user' ? 'Front' : 'Back')+' camera ready. Face ko frame ke center mein rakhein.';
    startAutoPreviewCapture();
  }catch(error){ status.textContent='Camera permission/error: '+error.message; }
  finally { cameraStartInProgress=false; }
}
async function flipCamera(){
  cameraFacingMode=cameraFacingMode==='user' ? 'environment' : 'user';
  if(cameraStream){ cameraStream.getTracks().forEach(track=>track.stop()); cameraStream=null; }
  await startCamera();
}
// Final front-facing frame ko high resolution mein crop/alignment karke
// backend ArcFace verification ke liye hidden form field mein rakhta hai.
async function captureFace(){
  const video=document.getElementById('camera'), canvas=document.getElementById('snapshot');
  if(!cameraStream){ document.getElementById('cameraStatus').textContent='Camera start nahi hua. Start live attendance dobara try karein.'; return; }
  if(!video.videoWidth || !video.videoHeight){
    document.getElementById('cameraStatus').textContent='Camera frame abhi ready nahi hai. Ek second ruk kar dobara try karein.';
    document.getElementById('captureStatus').textContent='Image capture: waiting for camera frame';
    return null;
  }
  document.getElementById('cameraStatus').textContent='High-quality face crop capture ho raha hai...';
  document.getElementById('captureStatus').textContent='Image capture: processing...';
  const source=document.createElement('canvas');
  source.width=video.videoWidth; source.height=video.videoHeight;
  source.getContext('2d').drawImage(video,0,0,source.width,source.height);
  document.getElementById('faceSourceImageBase64').value=source.toDataURL('image/jpeg',0.92);
  // Tracking 160px par fast hai, lekin final embedding ke landmarks frozen
  // full-resolution frame par 320px detector se nikalne chahiye.
  let captureFaces;
  try {
    captureFaces=await faceapi.detectAllFaces(source,captureDetectorOptions).withFaceLandmarks();
  } catch(error) {
    document.getElementById('cameraStatus').textContent='Image capture failed: '+error.message;
    document.getElementById('captureStatus').textContent='Image capture: failed';
    return null;
  }
  const detected=captureFaces.length===1 ? captureFaces[0] : null;
  if(detected){
    const finalQuality=checkFrameQuality(source,detected);
    const finalPose=validateFacePose(detected,false);
    const rejection=!finalQuality.ok ? finalQuality : finalPose;
    if(!rejection.ok){
      document.getElementById('faceImageBase64').value='';
      canvas.style.display='none';
      setCheck(!finalQuality.ok ? 'checkQuality' : 'checkPose','fail',!finalQuality.ok ? 'Quality: final capture failed' : 'Pose: final capture failed');
      document.getElementById('cameraStatus').textContent='Capture rejected: '+rejection.message;
      document.getElementById('captureStatus').textContent='Image capture: rejected — '+rejection.message;
      return null;
    }
  }
  if(detected){
    const aligned=alignFace(source,detected.landmarks);
    document.getElementById('faceImageBase64').value=aligned.toDataURL('image/jpeg',0.98);
    canvas.width=112; canvas.height=112;
    canvas.getContext('2d').drawImage(aligned,0,0);
  } else {
    document.getElementById('faceImageBase64').value='';
    document.getElementById('faceSourceImageBase64').value='';
    canvas.getContext('2d').clearRect(0,0,canvas.width,canvas.height);
    canvas.style.display='none';
  }
  if(detected) canvas.style.display='block';
  document.getElementById('cameraStatus').textContent=detected ? 'Image captured successfully. Face crop preview neeche dikh raha hai.'
    : captureFaces.length>1 ? 'Capture rejected: camera mein ek se zyada faces hain.' : 'Clear face detect nahi hua. Light aur camera distance check karein.';
  document.getElementById('captureStatus').textContent=detected ? 'Image capture: successful ✓' : 'Image capture: face not found';
  return detected ? document.getElementById('faceImageBase64').value : null;
}
async function captureCurrentFrameForTest(){
  if(!faceModelsReady){
    document.getElementById('cameraStatus').textContent='Face model abhi load ho raha hai.';
    return;
  }
  await startCamera();
  const captured=await captureFace();
  if(captured) document.getElementById('cameraStatus').textContent='Test capture successful. Ab live attendance/enrollment start kar sakte hain.';
}
function alignFace(source, landmarks){
  // ArcFace canonical eye positions ke saath similarity transform use hota
  // hai. Isme shear nahi hota, isliye seedha face tedha/cross nahi dikhega.
  const mean=points => ({x:points.reduce((sum,p)=>sum+p.x,0)/points.length,y:points.reduce((sum,p)=>sum+p.y,0)/points.length});
  const firstEye=mean(landmarks.getLeftEye()), secondEye=mean(landmarks.getRightEye());
  // Landmark naming camera/mirror ke hisaab se उलट सकती है. Image ke X
  // coordinate se order fix karne par accidental rotation nahi hota.
  const leftEye=firstEye.x<=secondEye.x ? firstEye : secondEye;
  const rightEye=firstEye.x<=secondEye.x ? secondEye : firstEye;
  const sourceCenter={x:(leftEye.x+rightEye.x)/2,y:(leftEye.y+rightEye.y)/2};
  const sourceDistance=Math.hypot(rightEye.x-leftEye.x,rightEye.y-leftEye.y);
  let angle=Math.atan2(rightEye.y-leftEye.y,rightEye.x-leftEye.x);
  // Straight face par landmark ke 2-3 degree noise se crop tedha dikhta tha.
  if(Math.abs(angle)<0.06) angle=0;
  const targetLeft={x:38.2946,y:51.6963}, targetRight={x:73.5318,y:51.5014};
  const targetCenter={x:(targetLeft.x+targetRight.x)/2,y:(targetLeft.y+targetRight.y)/2};
  const targetDistance=Math.hypot(targetRight.x-targetLeft.x,targetRight.y-targetLeft.y);
  const output=document.createElement('canvas'); output.width=112; output.height=112;
  const context=output.getContext('2d');
  context.imageSmoothingEnabled=true;
  context.imageSmoothingQuality='high';
  context.translate(targetCenter.x,targetCenter.y);
  context.rotate(-angle);
  context.scale(targetDistance/sourceDistance,targetDistance/sourceDistance);
  context.drawImage(source,-sourceCenter.x,-sourceCenter.y);
  return output;
}
function startAutoPreviewCapture(){
  if(previewCaptureTimer) clearInterval(previewCaptureTimer);
  previewFrameBusy=false;
  previewCaptureTimer=setInterval(async function(){
    const video=document.getElementById('camera');
    if(previewFrameBusy || autoCaptureTimer || !backendAvailable || !faceModelsReady || !cameraStream || !video.videoWidth) return;
    previewFrameBusy=true;
    try {
      const faces=await faceapi.detectAllFaces(video,detectorOptions).withFaceLandmarks();
      if(faces.length!==1){
        setCheck('checkFaceCount','fail',faces.length===0 ? 'Face: not found' : 'Face: multiple found');
        setCheck('checkPosition','pending','Position: waiting for one face');
        setCheck('checkQuality','pending','Quality: waiting');
        setCheck('checkPose','pending','Pose: live start ke baad');
        setCheck('checkStable','pending','Stable: live start ke baad');
        document.getElementById('captureStatus').textContent='Auto capture: single face ka wait ho raha hai...';
        return;
      }
      const detection=faces[0];
      setCheck('checkFaceCount','pass','Face: exactly one');
      const inside=isFaceInGuide(detection.detection.box,video,0);
      document.getElementById('faceGuide').style.borderColor=inside ? '#22c55e' : '#facc15';
      if(!inside){
        setCheck('checkPosition','fail','Position: outside guide');
        setCheck('checkQuality','pending','Quality: position ke baad');
        document.getElementById('captureStatus').textContent='Auto capture: face ko yellow circle ke andar rakhein...';
        return;
      }
      setCheck('checkPosition','pass','Position: passed');
      const quality=checkFrameQuality(video,detection);
      setCheck('checkQuality',quality.ok ? 'pass' : 'fail',quality.ok ? 'Quality: passed' : 'Quality: '+quality.message);
      document.getElementById('captureStatus').textContent='Live checks start karne ke liye Start live button dabayein...';
    } catch(error) {
      document.getElementById('captureStatus').textContent='Auto capture error: '+error.message;
    } finally { previewFrameBusy=false; }
  },350);
}
function stopAutoPreviewCapture(){
  if(previewCaptureTimer){ clearInterval(previewCaptureTimer); previewCaptureTimer=null; }
  previewFrameBusy=false;
}
async function startLiveAttendance(){
  if(submitting || liveStartInProgress) {
    console.info('Duplicate live capture start ignored', {submitting, liveStartInProgress});
    return;
  }
  liveStartInProgress=true;
  try {
    if(!await checkBackendHealth()) return;
    return await startLiveAttendanceOnce();
  } finally {
    liveStartInProgress=false;
  }
}
async function startLiveAttendanceOnce(){
  if(submitting) return;
  stopAutoPreviewCapture();
  const status=document.getElementById('cameraStatus'), button=document.getElementById('liveAttendanceButton');
  console.info('Live capture start requested', {
    uiVersion:'attendance-ui-2026-09-29-1505',
    userCode:document.getElementById('userCode').value,
    faceModelsReady,
    cameraReady:Boolean(cameraStream),
    secureContext:window.isSecureContext
  });
  if(!faceModelsReady){
    status.textContent='Face model load ho raha hai—live capture start kar raha hoon...';
    await loadFaceModels();
    if(!faceModelsReady){
      status.textContent='Face model ready nahi hua. Browser console/model error check karein.';
      button.disabled=false;
      return;
    }
  }
  button.disabled=true;
  console.info('Live capture starting', {userCode:document.getElementById('userCode').value, enrollmentMode});
  if(!enrollmentMode){
    // Never carry a proof from a previous page/server session into a new try.
    livenessAttemptActive=false;
    livenessSessionId=null;
    document.getElementById('livenessSessionId').value='';
  }
  const userCode=document.getElementById('userCode').value;
  // Enrollment status, camera aur GPS ko parallel start karke initial wait
  // kam rakha gaya hai.
  // Har punch ke liye fresh GPS lo. Page load par mila hua gpsReady value
  // IN ke baad OUT punch mein stale coordinates reuse kar sakta tha.
  const gpsPromise=enrollmentMode ? Promise.resolve(true) : getLocation();
  try {
    const statusPromise=fetch('${pageContext.request.contextPath}/api/v1/face/status?userCode='+encodeURIComponent(userCode));
    if(!cameraStream) await startCamera();
    if(!cameraStream) throw new Error('Camera ready nahi hai. HTTPS/camera permission check karein.');
    const response=await statusPromise;
    if(!response.ok) throw new Error('Face status request failed ('+response.status+')');
    currentEnrollmentReady=(await response.json()).verified;
  } catch(error) {
    status.textContent='Face enrollment status check failed.';
    button.disabled=false;
    return;
  }
  if(!currentEnrollmentReady && !enrollmentMode){
    // First-time user: same button starts the enrollment liveness challenge.
    enrollmentMode=true;
    status.textContent='Face enrolled nahi hai. Live enrollment start ho raha hai...';
  }
  if(currentEnrollmentReady && !await gpsPromise){
    status.textContent='Current GPS nahi mila. Location permission allow karke dobara try karein.';
    button.disabled=false;
    return;
  }
  if(enrollmentMode) currentEnrollmentReady=false;
  if(currentEnrollmentReady || enrollmentMode) {
    button.textContent=enrollmentMode ? 'Start live enrollment' : 'Start live attendance';
    // A failed/restarted server must never leave an old proof in the form.
    livenessSessionId=null;
    document.getElementById('livenessSessionId').value='';
    try {
      const livenessResponse=await fetch('${pageContext.request.contextPath}/api/v1/liveness/start?userCode='+encodeURIComponent(userCode),{method:'POST'});
      if(!livenessResponse.ok) throw new Error('challenge start failed');
      const challenge=await livenessResponse.json();
      livenessSessionId=challenge.sessionId;
      livenessNonce=challenge.nonce;
      livenessUserCode=userCode;
      livenessAttemptActive=true;
      serverFrameSequence=0; serverLivenessPassed=false;
      poseChallenges=challenge.sequence;
      completedChallenges=[];
      livenessStartedAt=Date.now();
      document.getElementById('livenessSessionId').value='';
    } catch(error) {
      livenessSessionId=null;
      livenessNonce=null; serverLivenessPassed=false;
      if(serverFrameTimer){ clearInterval(serverFrameTimer); serverFrameTimer=null; }
      livenessAttemptActive=false;
      document.getElementById('livenessSessionId').value='';
      status.textContent='Secure liveness challenge start nahi hua. Dobara try karein.';
      button.disabled=false;
      return;
    }
  } else {
    currentEnrollmentReady=false;
  }
  if(!cameraStream) await startCamera();
  if(!cameraStream){ button.disabled=false; return; }
  if(livenessSessionId) startServerFrameStream();
  if(autoCaptureTimer) clearInterval(autoCaptureTimer);
  poseStage=0; stableSince=0; centerYaw=0; centerPitch=0; centerFaceX=0; centerFaceY=0;
  // Enrollment aur attendance dono same server-issued sequence follow karte
  // hain. Enrollment ke liye session ko null karna server frame stream ko
  // band kar deta tha, jiski wajah se live enrollment kabhi complete nahi hota.
  frameBusy=false; clientFaceDetected=false; livenessFinishStarted=false; centerFaceImageBase64=null; lastFaceCenter=null; blinkOpenSeen=false; blinkClosed=false; blinkVerified=false; blinkOpenBaseline=0; blinkOpenFrames=0; blinkClosedFrames=0; blinkReopenFrames=0; blinkLogCounter=0;
  document.getElementById('captureStatus').textContent='Image capture: waiting for stable face';
  document.getElementById('snapshot').style.display='none';
  // Purana preview/attempt kabhi final attendance mein reuse nahi hoga.
  document.getElementById('faceImageBase64').value='';
  document.getElementById('faceSourceImageBase64').value='';
  resetCaptureChecks();
  document.getElementById('poseInstruction').textContent='Face center mein stable rakhein';
  status.textContent='Face ko yellow circle ke andar rakhein...';
  autoCaptureTimer=setInterval(async function(){
    const video=document.getElementById('camera');
    if(!cameraStream || !video.videoWidth || !faceModelsReady) return;
    if(frameBusy) return;
    frameBusy=true;
    try {
    // Exactly-one-face validation har processed frame par mandatory hai.
    const detections=await faceapi.detectAllFaces(video,detectorOptions).withFaceLandmarks();
    const detection=detections.length===1 ? detections[0] : null;
    if(detections.length!==1){
      clientFaceDetected=false;
      setCheck('checkFaceCount','fail',detections.length===0 ? 'Face: not found' : 'Face: multiple found');
      setCheck('checkPosition','pending','Position: waiting');
      setCheck('checkQuality','pending','Quality: waiting');
      setCheck('checkPose','pending','Pose: waiting');
      resetStableCheck();
      status.textContent=detections.length===0 ? 'Face detect nahi hua...' : 'Sirf ek face camera ke saamne rakhein...';
      return;
    }
    setCheck('checkFaceCount','pass','Face: exactly one');
    if(detection){
      // Blink ko quality/guide checks ke baad karne par eyelid-close frame
      // reject ho sakta tha. Exactly-one-face + landmarks milte hi sample
      // karo, taaki short blink miss na ho.
      // Allow blink detection even during center stage (poseStage===0) to capture early blinks
      updateBlink(detection.landmarks);
      // Center stage mein yellow oval strict rakhein. Head rotation/blink
      // challenge mein face naturally move hota hai, isliye strict oval se
      // challenge frame reject nahi karna hai.
      const faceInsideGuide=poseStage===0
        ? isFaceInGuide(detection.detection.box, video, 0)
        : isFaceInChallengeArea(detection.detection.box, video);
      clientFaceDetected=faceInsideGuide;
      document.getElementById('faceGuide').style.borderColor=faceInsideGuide ? '#22c55e' : '#facc15';
      if(!faceInsideGuide){
        setCheck('checkPosition','fail','Position: outside guide'); resetStableCheck();
        status.textContent='Face ko yellow circle ke bilkul andar rakhein...';
        return;
      }
      setCheck('checkPosition','pass','Position: passed');
      // Full brightness/contrast/sharpness scan sirf final front frame par
      // zaroori hai. Liveness frames mein ye scan har 90ms par chal raha tha
      // aur blink response ko unnecessarily delay kar raha tha.
      const quality=poseStage===0 ? checkFrameQuality(video,detection) : {ok:true};
      if(!quality.ok){ setCheck('checkQuality','fail','Quality: failed'); resetStableCheck(); status.textContent=quality.message; return; }
      setCheck('checkQuality','pass','Quality: passed');
      if(poseStage===0){
        const poseQuality=validateFacePose(detection,false);
        if(!poseQuality.ok){ setCheck('checkPose','fail','Pose: failed'); resetStableCheck(); status.textContent=poseQuality.message; return; }
      } else {
        // Stable 1s sirf front-face capture ke liye hai. Blink/head movement
        // challenge mein ise 0 reset karna user ko galat feedback deta tha.
        setCheck('checkStable','pass','Stable: ready');
      }
      setCheck('checkPose','pass',poseStage===0 ? 'Pose: front passed' : 'Pose: challenge');
      const center=detection.detection.box;
      const currentCenter={x:center.x+center.width/2,y:center.y+center.height/2};
      // Front stage mein face ko stable rakhna zaroori hai. Head LEFT/RIGHT
      // stages mein bounding box naturally move karta hai; us movement ko
      // reject karne se liveness motion kabhi complete nahi hota tha.
      const previousFaceCenter=lastFaceCenter;
      lastFaceCenter=currentCenter;
      if(previousFaceCenter && poseStage===0){
        const movement=Math.hypot(currentCenter.x-previousFaceCenter.x,currentCenter.y-previousFaceCenter.y);
        if(movement>video.videoWidth*0.04){
          resetStableCheck();
          document.getElementById('poseInstruction').textContent='Face stable rakhein — checking again';
          status.textContent='Face ko stable rakhein...';
          return;
        }
      }
      const yaw=estimateYaw(detection.landmarks);
      const pitch=estimatePitch(detection.landmarks);
      const challenge=poseChallenges[poseStage-1];
      // Front frame guide, quality aur two-frame stability se validate ho
      // chuka hai. Absolute yaw person/camera ke अनुसार बदलता है, इसलिए उसी
      // unreliable zero-value पर center capture को block नहीं करना है.
      const poseCorrect = poseStage===0 ? true
        : isPoseChallengeSatisfied(challenge, yaw, pitch, currentCenter, video);
      if(poseCorrect){
        status.textContent=poseStage===0 ? 'Front face capture ho raha hai...' : 'Movement detected...';
        if(poseStage===0 && !stableSince) stableSince=Date.now();
        const stableForMs=poseStage===0 ? Date.now()-stableSince : FRONT_STABLE_MILLIS;
        const stablePercent=Math.min(100,Math.round(stableForMs/10));
        setCheck('checkStable',stableForMs>=FRONT_STABLE_MILLIS ? 'pass' : 'pending','Stable '+(FRONT_STABLE_MILLIS/1000)+'s: '+stablePercent+'%');
        if(poseStage===0) document.getElementById('poseInstruction').textContent='Face quality passed — stable '+stablePercent+'%';
        if(poseStage!==0 || stableForMs>=FRONT_STABLE_MILLIS){
          if(poseStage===0){
            const centerCapture=await captureFace();
            centerFaceImageBase64=document.getElementById('faceImageBase64').value;
            if(!centerCapture){
              poseStage=0; resetStableCheck();
              return;
            }
          }
          if(poseStage===0){
            centerYaw=yaw; centerPitch=pitch;
            centerFaceX=currentCenter.x; centerFaceY=currentCenter.y;
            // Preserve blink state when transitioning to BLINK challenge
            // If user already blinked during center stage, don't reset it
            if(poseChallenges[0]!=='BLINK'){
              blinkOpenSeen=false; blinkClosed=false; blinkVerified=false;
            }
          } else {
            completedChallenges.push(poseChallenges[poseStage-1]);
          }
          poseStage++;
          const totalStages=1+poseChallenges.length;
          if(poseStage<totalStages){
            setCheck('checkLiveness','pending','Liveness: '+poseChallenges[poseStage-1]+' pending');
            document.getElementById('poseInstruction').textContent=challengeInstruction(poseChallenges[poseStage-1]);
          } else {
            clearInterval(autoCaptureTimer); autoCaptureTimer=null;
            document.getElementById('poseInstruction').textContent='Liveness verified';
            setCheck('checkLiveness','pass','Liveness: passed');
            // Turned frame sirf liveness ke liye tha. Recognition ke liye
            // stable, front-facing center frame hi use hoga.
            if(!await waitForServerLiveness(status)){
              setCheck('checkLiveness','fail','Liveness: server rejected');
              status.textContent='Server ne liveness frames accept nahi kiye. Dobara try karein.';
              button.disabled=false;
              return;
            }
            if(!livenessFinishStarted){
              livenessFinishStarted=true;
              await finishAttendanceOrEnrollment(centerFaceImageBase64, status, button);
            }
          }
        }
      } else {
        if(poseStage===0) resetStableCheck();
        if(poseStage>0) setCheck('checkStable','pass','Stable: ready');
        status.textContent=poseStage===0 ? 'Face quality checks complete hone dein...'
          : challengeInstruction(poseChallenges[poseStage-1])+'...';
      }
    } else {
      document.getElementById('faceGuide').style.borderColor='#facc15';
      status.textContent='Face detect nahi hua...';
    }
    } catch(error) {
      setCheck('checkFaceCount','fail','Face: detector error');
      setCheck('checkPosition','pending','Position: waiting');
      setCheck('checkQuality','pending','Quality: waiting');
      status.textContent='Face detection error: '+error.message+' — camera ko stable rakhein aur dobara try karein.';
    } finally {
      frameBusy=false;
    }
  },LOCAL_FACE_CHECK_INTERVAL_MS);
}
function checkFrameQuality(video, detection){
  const box=detection.detection.box;
  const sourceWidth=video.videoWidth || video.naturalWidth || video.width;
  const sourceHeight=video.videoHeight || video.naturalHeight || video.height;
  if(!sourceWidth || !sourceHeight) return {ok:false,message:'Image dimensions read nahi ho rahe hain.'};
  if(box.width<sourceWidth*0.15) return {ok:false,message:'Face bahut door/chhota hai—camera ke paas aaiye.'};
  if(box.width>sourceWidth*0.80) return {ok:false,message:'Face camera ke bahut paas hai—thoda door jaiye.'};
  const canvas=document.createElement('canvas'); canvas.width=96; canvas.height=72;
  const context=canvas.getContext('2d'); context.drawImage(video,box.x,box.y,box.width,box.height,0,0,96,72);
  const pixels=context.getImageData(0,0,96,72).data, grayValues=new Float32Array(96*72); let brightness=0, contrast=0, previous=0;
  for(let i=0,p=0;i<pixels.length;i+=4,p++){ const gray=0.299*pixels[i]+0.587*pixels[i+1]+0.114*pixels[i+2]; grayValues[p]=gray; brightness+=gray; if(p>0) contrast+=Math.abs(gray-previous); previous=gray; }
  brightness/=pixels.length/4; contrast/=pixels.length/4;
  if(brightness<45) return {ok:false,message:'Face par light kam hai.'};
  if(brightness>225) return {ok:false,message:'Face par light bahut bright hai.'};
  if(contrast<7) return {ok:false,message:'Image blur/low quality hai—camera stable rakhein.'};
  let laplacianSum=0, laplacianSquareSum=0, laplacianCount=0;
  for(let y=1;y<71;y++) for(let x=1;x<95;x++){
    const index=y*96+x;
    const laplacian=grayValues[index-96]+grayValues[index+96]+grayValues[index-1]+grayValues[index+1]-4*grayValues[index];
    laplacianSum+=laplacian; laplacianSquareSum+=laplacian*laplacian; laplacianCount++;
  }
  const laplacianMean=laplacianSum/laplacianCount;
  const sharpness=laplacianSquareSum/laplacianCount-laplacianMean*laplacianMean;
  if(sharpness<20) return {ok:false,message:'Face blurred hai (sharpness '+Math.round(sharpness)+'/20)—camera stable rakhein aur focus hone dein.'};
  const leftEye=detection.landmarks.getLeftEye(), rightEye=detection.landmarks.getRightEye();
  if(leftEye.length<6 || rightEye.length<6) return {ok:false,message:'Dono eyes clearly visible rakhein.'};
  return {ok:true};
}
function validateEnrollmentFace(source, detection){
  const quality=checkFrameQuality(source,detection);
  if(!quality.ok) return quality;
  return validateFacePose(detection,true);
}
function validateFacePose(detection, checkAbsoluteYaw){
  const left=detection.landmarks.getLeftEye(), right=detection.landmarks.getRightEye();
  const mean=points => ({x:points.reduce((sum,p)=>sum+p.x,0)/points.length,y:points.reduce((sum,p)=>sum+p.y,0)/points.length});
  const first=mean(left), second=mean(right);
  const imageLeft=first.x<=second.x ? first : second, imageRight=first.x<=second.x ? second : first;
  const roll=Math.atan2(imageRight.y-imageLeft.y,imageRight.x-imageLeft.x);
  if(Math.abs(roll)>0.26) return {ok:false,message:'Face lagbhag 15 degree se zyada tilted hai—thoda seedha karein.'};
  if(checkAbsoluteYaw && Math.abs(estimateYaw(detection.landmarks))>0.55) return {ok:false,message:'Face side pose mein hai—seedha camera dekhein.'};
  return {ok:true,message:'Face quality valid hai.'};
}
function updateBlink(landmarks){
  // Allow blink detection during center stage (poseStage===0) to capture early blinks
  // and preserve blink state when transitioning to BLINK challenge
  const currentChallenge=poseStage>0 ? poseChallenges[poseStage-1] : null;
  if(currentChallenge!=='BLINK' && poseStage!==0) return;
  const ear=eyeRatio(landmarks.getLeftEye(),landmarks.getRightEye());
  lastBlinkEar=ear;
  // First learn an open-eye baseline. Then require a short closed phase and
  // an explicit reopen phase; this prevents a single noisy frame from passing.
  if(!blinkClosed && ear>0.12){
    blinkOpenFrames++;
    if(blinkOpenFrames>=2){
      blinkOpenBaseline=blinkOpenBaseline ? Math.max(blinkOpenBaseline*0.90+ear*0.10,ear) : ear;
      blinkOpenSeen=true;
    }
  } else if(!blinkClosed) blinkOpenFrames=0;
  // A blink can reduce one eye's EAR more than the other; use a relaxed
  // relative threshold, then require the eyes to reopen for two frames.
  const closedThreshold=Math.max(0.10,Math.min(0.24,blinkOpenBaseline*0.84));
  const reopenThreshold=Math.max(0.13,blinkOpenBaseline*0.80);
  if(blinkOpenSeen && !blinkClosed && ear<closedThreshold){
    blinkClosedFrames++;
    if(blinkClosedFrames>=1) blinkClosed=true;
  } else if(!blinkClosed) blinkClosedFrames=0;
  if(blinkClosed && !blinkVerified){
    if(ear>reopenThreshold) blinkReopenFrames++; else blinkReopenFrames=0;
    if(blinkReopenFrames>=2) blinkVerified=true;
  }
  if(currentChallenge==='BLINK' && blinkOpenBaseline){
    blinkLogCounter++;
    if(blinkLogCounter%5===0) console.info('Blink debug', {ear:Number(ear.toFixed(3)),baseline:Number(blinkOpenBaseline.toFixed(3)),closedThreshold:Number(closedThreshold.toFixed(3)),reopenThreshold:Number(reopenThreshold.toFixed(3)),openSeen:blinkOpenSeen,closed:blinkClosed,verified:blinkVerified});
    const blinkText=blinkVerified ? 'Blink: passed ✓' : blinkClosed ? 'Blink: aankhen phir kholen' : 'Blink: aankhen band karein';
    setCheck('checkLiveness',blinkVerified ? 'pass' : 'pending',blinkText);
    if(!blinkVerified) document.getElementById('cameraStatus').textContent=blinkText+' (EAR '+ear.toFixed(2)+', baseline '+blinkOpenBaseline.toFixed(2)+')';
  }
}
function eyeRatio(left,right){
  const ratio=eye => (Math.hypot(eye[1].x-eye[5].x,eye[1].y-eye[5].y)+Math.hypot(eye[2].x-eye[4].x,eye[2].y-eye[4].y))/(2*Math.hypot(eye[0].x-eye[3].x,eye[0].y-eye[3].y));
  // One eye can be partially occluded or tracked with more noise. The lower
  // eye EAR is the useful signal for an eye-close event.
  return Math.min(ratio(left),ratio(right));
}
function estimateYaw(landmarks){
  const leftEye=landmarks.getLeftEye(), rightEye=landmarks.getRightEye(), nose=landmarks.getNose();
  const meanX=points => points.reduce((sum,p)=>sum+p.x,0)/points.length;
  const eyeLeft=meanX(leftEye), eyeRight=meanX(rightEye), eyeCenter=(eyeLeft+eyeRight)/2, eyeDistance=Math.abs(eyeRight-eyeLeft);
  const noseX=meanX(nose), ratio=(noseX-eyeCenter)/eyeDistance;
  return eyeDistance ? ratio : 0;
}
function estimatePitch(landmarks){
  const mean=points => points.reduce((sum,p)=>sum+p.y,0)/points.length;
  const eyeY=(mean(landmarks.getLeftEye())+mean(landmarks.getRightEye()))/2;
  const mouthY=mean(landmarks.getMouth());
  const noseY=mean(landmarks.getNose());
  const faceHeight=Math.max(1,mouthY-eyeY);
  return (noseY-eyeY)/faceHeight;
}
function isPoseChallengeSatisfied(challenge, yaw, pitch, currentCenter, video){
  if(challenge==='BLINK') return blinkVerified;
  const desired=(challenge==='LEFT' || challenge==='UP') ? -1 : 1;
  const xDelta=(currentCenter.x-centerFaceX)/video.videoWidth;
  const yDelta=(currentCenter.y-centerFaceY)/video.videoHeight;
  const yawDelta=yaw-centerYaw;
  const pitchDelta=pitch-centerPitch;
  if(challenge==='LEFT' || challenge==='RIGHT'){
    const requestedDirection=(Math.abs(xDelta)>=0.022 && Math.sign(xDelta)===desired)
      || (Math.abs(yawDelta)>=0.030 && Math.sign(yawDelta)===desired);
    // Front cameras mirrored/non-mirrored ho sakte hain. Clear strong motion
    // ko sign mismatch ke कारण indefinitely block nahi karna hai.
    return requestedDirection || Math.abs(xDelta)>=0.055 || Math.abs(yawDelta)>=0.075;
  }
  const requestedDirection=(Math.abs(yDelta)>=0.022 && Math.sign(yDelta)===desired)
    || (Math.abs(pitchDelta)>=0.040 && Math.sign(pitchDelta)===desired);
  return requestedDirection || Math.abs(yDelta)>=0.055 || Math.abs(pitchDelta)>=0.090;
}
function isFaceInGuide(box, video, stage){
  // Video par object-fit:cover laga hai. Isliye camera ke source pixels aur
  // screen par dikhne wale yellow guide ke coordinates same nahi hote (mostly
  // mobile 16:9 video ko 4:3 preview mein horizontal crop kiya jata hai).
  // Detection ko ab actual displayed guide ke against compare karte hain.
  const videoRect=video.getBoundingClientRect();
  const guideRect=document.getElementById('faceGuide').getBoundingClientRect();
  if(!videoRect.width || !videoRect.height || !guideRect.width || !guideRect.height) return false;
  const scale=Math.max(videoRect.width/video.videoWidth,videoRect.height/video.videoHeight);
  const renderedWidth=video.videoWidth*scale, renderedHeight=video.videoHeight*scale;
  const cropX=(renderedWidth-videoRect.width)/2, cropY=(renderedHeight-videoRect.height)/2;
  const faceX=videoRect.left+(box.x+box.width/2)*scale-cropX;
  const faceY=videoRect.top+(box.y+box.height/2)*scale-cropY;
  const guideCenterX=guideRect.left+guideRect.width/2;
  const guideCenterY=guideRect.top+guideRect.height/2;
  const radiusX=guideRect.width/2, radiusY=guideRect.height/2;
  const normalizedX=(faceX-guideCenterX)/radiusX;
  const normalizedY=(faceY-guideCenterY)/radiusY;
  // Detector box aur visible face oval exact same boundary nahi hote; small
  // tolerance se circle ke edge par valid face reject nahi hoga.
  const insideGuide=normalizedX*normalizedX+normalizedY*normalizedY<=1.20;
  const displayedFaceWidth=box.width*scale;
  const minimumFaceWidth=videoRect.width*(stage===0 ? 0.07 : 0.05);
  const maximumFaceWidth=videoRect.width*(stage===0 ? 0.88 : 0.95);
  return insideGuide && displayedFaceWidth>=minimumFaceWidth && displayedFaceWidth<=maximumFaceWidth;
}
function isFaceInChallengeArea(box, video){
  const centerX=(box.x+box.width/2)/video.videoWidth;
  const centerY=(box.y+box.height/2)/video.videoHeight;
  const width=box.width/video.videoWidth;
  const height=box.height/video.videoHeight;
  return centerX>=0.04 && centerX<=0.96 && centerY>=0.08 && centerY<=0.92
    && width>=0.05 && height>=0.05 && width<=0.98 && height<=0.98;
}
function startServerFrameStream(){
  if(serverFrameTimer) clearInterval(serverFrameTimer);
  serverFrameTimer=setInterval(sendServerLivenessFrame,SERVER_FACE_CHECK_INTERVAL_MS);
}
async function sendServerLivenessFrame(){
  // Browser face detection must confirm one usable face before any frame is
  // sent to the backend. No face means no server call.
  if(serverFrameBusy || serverLivenessPassed || !clientFaceDetected || !cameraStream || !livenessSessionId || !livenessNonce) return;
  const video=document.getElementById('camera');
  if(!video.videoWidth || !video.videoHeight) return;
  serverFrameBusy=true;
  const canvas=document.createElement('canvas'); canvas.width=320; canvas.height=240;
  canvas.getContext('2d').drawImage(video,0,0,canvas.width,canvas.height);
  const sequenceNumber=++serverFrameSequence;
  const abortController=new AbortController();
  const timeoutId=setTimeout(()=>abortController.abort(),5000);
  try {
    const response=await fetch('${pageContext.request.contextPath}/api/v1/liveness/frame',{
      method:'POST',headers:{'Content-Type':'application/json'},
      body:JSON.stringify({sessionId:livenessSessionId,userCode:livenessUserCode,nonce:livenessNonce,
        sequenceNumber,capturedAtMillis:Date.now(),frameImageBase64:canvas.toDataURL('image/jpeg',0.72)}),
      signal:abortController.signal
    });
    const responseText=await response.text();
    let result;
    try { result=JSON.parse(responseText); }
    catch(error) { result={message:responseText || 'Server returned an unreadable rejection.'}; }
    if(!response.ok || result.accepted===false){
      const message=result.message || result.error || 'Server frame rejected.';
      document.getElementById('cameraStatus').textContent='Server liveness: '+message;
      setCheck('checkLiveness','fail','Liveness: server frame rejected');
      console.warn('Server liveness frame rejected', {status:response.status, reason:message, response:result});
      return;
    }
    if(response.ok && result.passed){
      serverLivenessPassed=true;
      setCheck('checkLiveness','pass','Liveness: server passed');
      if(serverFrameTimer){ clearInterval(serverFrameTimer); serverFrameTimer=null; }
      // Server has verified the complete liveness sequence. Do not wait for
      // the browser's less reliable pose heuristic to finish again.
      if(centerFaceImageBase64 && !livenessFinishStarted){
        livenessFinishStarted=true;
        if(autoCaptureTimer){ clearInterval(autoCaptureTimer); autoCaptureTimer=null; }
        const finishStatus=document.getElementById('cameraStatus');
        const finishButton=document.getElementById('liveAttendanceButton');
        await finishAttendanceOrEnrollment(centerFaceImageBase64, finishStatus, finishButton);
      }
    } else if(response.ok){
      const stage=result.stage+'/'+result.totalStages;
      document.getElementById('cameraStatus').textContent='Server liveness checking: stage '+stage+' (frame '+sequenceNumber+')';
      setCheck('checkLiveness','pending','Server liveness: '+stage);
    }
  } catch(error) {
    const message=error.name==='AbortError' ? 'Server response timeout (5s).' : error.message;
    document.getElementById('cameraStatus').textContent='Server liveness connection error: '+message;
    setCheck('checkLiveness','fail','Liveness: server unavailable');
    console.warn('Server liveness frame error',error);
  } finally { clearTimeout(timeoutId); serverFrameBusy=false; }
}
async function waitForServerLiveness(status){
  const deadline=Date.now()+6000;
  status.textContent='Server liveness frames verify ho rahe hain...';
  while(Date.now()<deadline){
    if(serverLivenessPassed) return true;
    await new Promise(resolve=>setTimeout(resolve,150));
  }
  return serverLivenessPassed;
}
async function completeServerLivenessProof(){
  const response=await fetch('${pageContext.request.contextPath}/api/v1/liveness/complete',{
    method:'POST',headers:{'Content-Type':'application/json'},
    body:JSON.stringify({sessionId:livenessSessionId,userCode:livenessUserCode,completedSequence:[],clientDurationMillis:Date.now()-livenessStartedAt})
  });
  const result=await response.json();
  if(!response.ok || !result.verified) throw new Error(result.message || 'server liveness proof rejected');
  // Enrollment-first flow also uses this completed proof for the attendance
  // request that follows. Keep the server-issued session ID in the payload.
  document.getElementById('livenessSessionId').value=livenessSessionId;
}
async function finishAttendanceOrEnrollment(descriptor, status, button){
  if(!descriptor || !document.getElementById('faceImageBase64').value){ button.disabled=false; return; }
  if(centerFaceImageBase64) document.getElementById('faceImageBase64').value=centerFaceImageBase64;
  if(!currentEnrollmentReady){
    status.textContent=enrollmentMode ? 'Live face enrollment save ho raha hai...' : 'Face enrolled nahi hai. Pehle Face enrollment section se register karein.';
    if(enrollmentMode){
      let enrolled=false;
      try { await completeServerLivenessProof(); enrolled=await enrollFace(); }
      catch(error){ status.textContent='Server liveness rejected: '+error.message; }
      enrollmentMode=false;
      if(enrolled){
        // The user started an attendance flow, so do not stop after the
        // first-time enrollment. The completed liveness proof is consumed by
        // the attendance request below after the new embedding is saved.
        currentEnrollmentReady=true;
        status.textContent='Face enrollment saved. Attendance verify ho rahi hai...';
        submitting=true;
        await submitAttendanceForm(status);
      } else {
        status.textContent='Live face enrollment failed.';
        refreshEnrollmentStatus();
        button.disabled=false;
      }
      return;
    }
    button.disabled=false;
    return;
  }
  try {
    const selectedUser=document.getElementById('userCode').value;
    if(!livenessSessionId || livenessUserCode!==selectedUser)
      throw new Error('Selected user change hua hai. Naya liveness attempt start karein.');
    const proofResponse=await fetch('${pageContext.request.contextPath}/api/v1/liveness/complete',{
      method:'POST',headers:{'Content-Type':'application/json'},
      body:JSON.stringify({sessionId:livenessSessionId,userCode:selectedUser,
        completedSequence:completedChallenges,clientDurationMillis:Date.now()-livenessStartedAt})
    });
    const proof=await proofResponse.json();
    if(!proofResponse.ok || !proof.verified) throw new Error(proof.message || 'liveness proof rejected');
    document.getElementById('livenessSessionId').value=livenessSessionId;
  } catch(error) {
    setCheck('checkLiveness','fail','Liveness: server rejected');
    status.textContent='Attendance blocked: '+error.message;
    button.disabled=false;
    return;
  }
  submitting=true;
  status.textContent='Liveness aur face match ho raha hai, attendance submit ho rahi hai...';
  await submitAttendanceForm(status);
}
async function submitAttendanceForm(status){
  const form=document.querySelector('form');
  let latitude=document.getElementById('latitude').value;
  let longitude=document.getElementById('longitude').value;
  if(!latitude || !longitude || (Number(latitude)===0 && Number(longitude)===0)){
    status.textContent='GPS location abhi available nahi hai. Location permission allow karein...';
    if(!await getLocation()){
      submitting=false;
      status.textContent='GPS nahi mila. Browser mein Location permission allow karke dobara try karein.';
      return;
    }
    latitude=document.getElementById('latitude').value;
    longitude=document.getElementById('longitude').value;
  }
  const latitudeNumber=Number(latitude), longitudeNumber=Number(longitude);
  if(!Number.isFinite(latitudeNumber) || !Number.isFinite(longitudeNumber)
      || latitudeNumber < -90 || latitudeNumber > 90
      || longitudeNumber < -180 || longitudeNumber > 180
      || (latitudeNumber===0 && longitudeNumber===0)){
    submitting=false;
    status.textContent='Valid GPS coordinates nahi mile. Location permission allow karke dobara try karein.';
    return;
  }
  const payload=new URLSearchParams();
  // Explicit payload: browser ke native form serialization par depend nahi
  // karna hai, kyunki isi flow mein backend ko blank request mil rahi thi.
  payload.set('userCode',document.getElementById('userCode').value || '');
  payload.set('locationCode',document.getElementById('locationCode').value || '');
  payload.set('attendanceType',form.elements.attendanceType.value || '');
  payload.set('latitude',String(latitudeNumber));
  payload.set('longitude',String(longitudeNumber));
  payload.set('faceImageBase64',document.getElementById('faceImageBase64').value || '');
  payload.set('sourceImageBase64',document.getElementById('faceSourceImageBase64').value || '');
  payload.set('livenessSessionId',document.getElementById('livenessSessionId').value || '');
  try {
    // Saare fields ek hi standard form-urlencoded POST body mein bheje ja rahe
    // hain. Query string aur alag image body ke split flow se request
    // parameters lose ho rahe the.
    const submitUrl=form.action;
    console.info('Submitting attendance payload', {
      url:submitUrl,
      latitude:latitudeNumber,
      longitude:longitudeNumber,
      userCode:payload.get('userCode'),
      locationCode:payload.get('locationCode'),
      attendanceType:payload.get('attendanceType')
    });
    const response=await fetch(submitUrl,{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8','Accept':'text/html'},body:payload.toString()});
    if(!response.ok) throw new Error('Attendance request failed ('+response.status+')');
    document.open();
    document.write(await response.text());
    document.close();
  } catch(error) {
    submitting=false;
    status.textContent='Attendance submit failed: '+error.message;
  }
}
function startLiveEnrollment(){
  enrollmentMode=true;
  startLiveAttendance();
}
async function enrollFace(){
  const user=document.getElementById('userCode').value;
  const faceImage=document.getElementById('faceImageBase64').value;
  if(!faceImage){ document.getElementById('cameraStatus').textContent='Pehle face capture karein.'; return false; }
  document.getElementById('cameraStatus').textContent='Face server par enroll ho raha hai...';
  try {
    const response=await fetch('${pageContext.request.contextPath}/api/v1/face/enroll',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({userCode:user,faceImageBase64:faceImage,sourceImageBase64:document.getElementById('faceSourceImageBase64').value})});
    const result=await response.json();
    const success=response.ok && result.verified;
    document.getElementById('cameraStatus').textContent=success ? result.message : 'Enrollment failed: '+result.message;
    const enrollmentStatus=document.getElementById('enrollmentStatus');
    enrollmentStatus.textContent=success ? user+' face embedding database mein save ho gayi.' : 'Enrollment failed: '+result.message;
    enrollmentStatus.className=success ? 'ok' : 'bad';
    return success;
  } catch(error) { document.getElementById('cameraStatus').textContent='Enrollment request failed: '+error.message; }
  return false;
}
async function enrollUploadedFace(){
  const file=document.getElementById('enrollmentImage').files[0];
  const user=document.getElementById('userCode').value;
  const status=document.getElementById('enrollmentStatus'), button=document.getElementById('enrollImageButton');
  if(!file){ status.textContent='Pehle clear face image select karein.'; return; }
  if(!faceModelsReady){ status.textContent='Face model abhi ready nahi hai.'; return; }
  button.disabled=true; status.textContent='Face detect aur crop ho raha hai...';
  try {
    const image=await faceapi.bufferToImage(file);
    const faces=await faceapi.detectAllFaces(image,captureDetectorOptions).withFaceLandmarks();
    if(faces.length!==1){ status.textContent=faces.length===0 ? 'Image mein clear face nahi मिला.' : 'Image mein sirf ek face hona chahiye.'; button.disabled=false; return; }
    const detected=faces[0];
    const validation=validateEnrollmentFace(image,detected);
    if(!validation.ok){ status.textContent='Image rejected: '+validation.message; status.className='bad'; button.disabled=false; return; }
    status.textContent='Image quality valid. Face embedding save ho rahi hai...'; status.className='hint';
    const aligned=alignFace(image,detected.landmarks);
    const sourceCanvas=document.createElement('canvas');
    sourceCanvas.width=image.naturalWidth || image.width; sourceCanvas.height=image.naturalHeight || image.height;
    sourceCanvas.getContext('2d').drawImage(image,0,0,sourceCanvas.width,sourceCanvas.height);
    const response=await fetch('${pageContext.request.contextPath}/api/v1/face/enroll',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({userCode:user,faceImageBase64:aligned.toDataURL('image/jpeg',0.98),sourceImageBase64:sourceCanvas.toDataURL('image/jpeg',0.92)})});
    const result=await response.json();
    status.textContent=response.ok && result.verified ? 'Face enrollment saved successfully.' : 'Enrollment failed: '+(result.message || 'server error');
    if(response.ok && result.verified) refreshEnrollmentStatus();
  } catch(error) { status.textContent='Enrollment failed: '+error.message; }
  button.disabled=false;
}
async function refreshEnrollmentStatus(){
  const button=document.getElementById('liveAttendanceButton'), status=document.getElementById('cameraStatus'), enrollmentStatus=document.getElementById('enrollmentStatus');
  if(!faceModelsReady) return;
  try {
    const userCode=document.getElementById('userCode').value;
    const response=await fetch('${pageContext.request.contextPath}/api/v1/face/status?userCode='+encodeURIComponent(userCode));
    const enrolled=(await response.json()).verified;
    button.textContent=enrolled ? 'Start live attendance' : 'Face enrollment required';
    button.disabled=false;
    status.textContent=enrolled ? 'Face enrolled. Attendance ke liye ready.' : 'Face enrolled nahi hai. Live enrollment start karein.';
    enrollmentStatus.textContent=enrolled ? userCode+' face embedding database mein saved hai.' : userCode+' ka face abhi enrolled nahi hai.';
    enrollmentStatus.className=enrolled ? 'ok' : 'hint';
  } catch(error) { status.textContent='Face enrollment status check failed.'; }
}
function getLocation(){
  const status=document.getElementById('gpsStatus');
  if(!navigator.geolocation){ status.textContent=' GPS is not available on this device.'; return Promise.resolve(false); }
  status.textContent=' GPS location read ho rahi hai...';
  return new Promise(resolve => navigator.geolocation.getCurrentPosition(function(position){
    document.getElementById('latitude').value=position.coords.latitude;
    document.getElementById('longitude').value=position.coords.longitude;
    console.info('Current GPS coordinates loaded', {
      latitude:position.coords.latitude,
      longitude:position.coords.longitude,
      accuracyMeters:position.coords.accuracy
    });
    status.textContent=' Current GPS loaded (accuracy: '+Math.round(position.coords.accuracy)+'m).';
    resolve(true);
  }, function(error){
    console.warn('Current GPS coordinates unavailable', {code:error.code, message:error.message});
    status.textContent=' GPS permission/error: '+error.message;
    resolve(false);
  }, {enableHighAccuracy:true,timeout:15000,maximumAge:0}));
}
async function checkBackendHealth(){
  try {
    const response=await fetch('${pageContext.request.contextPath}/api/v1/system/health?ts='+Date.now(),{cache:'no-store'});
    backendAvailable=response.ok;
  } catch(error) {
    backendAvailable=false;
  }
  if(!backendAvailable){
    if(previewCaptureTimer){ clearInterval(previewCaptureTimer); previewCaptureTimer=null; }
    if(autoCaptureTimer){ clearInterval(autoCaptureTimer); autoCaptureTimer=null; }
    if(serverFrameTimer){ clearInterval(serverFrameTimer); serverFrameTimer=null; }
    if(cameraStream){ cameraStream.getTracks().forEach(track=>track.stop()); cameraStream=null; }
    const status=document.getElementById('cameraStatus');
    if(status) status.textContent='Backend server offline hai. Server start karke page refresh karein.';
    const gpsStatus=document.getElementById('gpsStatus');
    if(gpsStatus) gpsStatus.textContent='Backend unavailable — live detection paused.';
  }
  return backendAvailable;
}
function startBackendHealthMonitor(){
  if(backendHealthTimer) clearInterval(backendHealthTimer);
  backendHealthTimer=setInterval(checkBackendHealth,5000);
}
function selectAssignedLocation(){
  const user=document.querySelector('#userCode option:checked');
  if(user){
    document.getElementById('locationCode').value=user.dataset.assignedLocation;
    document.getElementById('enrollmentUserLabel').textContent=user.textContent.trim();
  }
  refreshEnrollmentStatus();
}
function handleUserChange(){
  if(autoCaptureTimer){ clearInterval(autoCaptureTimer); autoCaptureTimer=null; }
  if(serverFrameTimer){ clearInterval(serverFrameTimer); serverFrameTimer=null; }
  livenessSessionId=null; livenessNonce=null; livenessUserCode=null; livenessAttemptActive=false; serverLivenessPassed=false; completedChallenges=[];
  document.getElementById('livenessSessionId').value='';
  document.getElementById('faceImageBase64').value='';
  document.getElementById('faceSourceImageBase64').value='';
  document.getElementById('captureStatus').textContent='Image capture: not started';
  document.getElementById('snapshot').style.display='none';
  enrollmentMode=false; submitting=false;
  autoLiveKickoffStarted=false;
  resetCaptureChecks();
  document.getElementById('poseInstruction').textContent='Naye user ke liye Start live attendance karein';
  selectAssignedLocation();
  if(faceModelsReady) {
    autoLiveKickoffStarted=true;
    setTimeout(()=>startLiveAttendance().catch(error=>{
      console.error('User-change live capture start failed',error);
      document.getElementById('cameraStatus').textContent='Live capture start failed: '+error.message;
      document.getElementById('liveAttendanceButton').disabled=false;
    }),500);
  }
}
async function validatePunch(event){
  event.preventDefault();
  if(!document.getElementById('faceImageBase64').value){
    document.getElementById('cameraStatus').textContent='Punch se pehle face capture karein.';
    return false;
  }
  if(!livenessAttemptActive || !livenessSessionId || document.getElementById('livenessSessionId').value!==livenessSessionId){
    document.getElementById('cameraStatus').textContent='Current attempt ka liveness proof complete karein. Purana proof use nahi ho sakta.';
    return false;
  }
  if(!faceModelsReady){ document.getElementById('cameraStatus').textContent='Face capture/model ready nahi hai.'; return false; }
  document.getElementById('cameraStatus').textContent='Face image ready. Server verification ho rahi hai.';
  submitting=true;
  await submitAttendanceForm(document.getElementById('cameraStatus'));
  return false;
}
window.addEventListener('load',function(){
  console.info('Attendance UI loaded', {uiVersion:'attendance-ui-2026-09-29-1505', action:document.querySelector('form').action});
  document.querySelector('form').addEventListener('submit',function(event){
    event.preventDefault();
    console.warn('Native attendance form submit blocked; live capture must submit through JavaScript.');
  });
  document.getElementById('livenessSessionId').value='';
  livenessSessionId=null; livenessNonce=null; livenessAttemptActive=false; serverLivenessPassed=false;
  selectAssignedLocation(); getLocation();
  startBackendHealthMonitor();
  checkBackendHealth().then(available=>{ if(available) loadFaceModels(); });
});
window.addEventListener('unhandledrejection',function(event){
  console.error('Attendance UI unhandled promise rejection',event.reason);
  const status=document.getElementById('cameraStatus');
  if(status) status.textContent='Live capture error: '+(event.reason && event.reason.message ? event.reason.message : event.reason);
});
window.addEventListener('beforeunload',function(){
  if(previewCaptureTimer) clearInterval(previewCaptureTimer);
  if(autoCaptureTimer) clearInterval(autoCaptureTimer);
  if(serverFrameTimer) clearInterval(serverFrameTimer);
  if(backendHealthTimer) clearInterval(backendHealthTimer);
  if(cameraStream) cameraStream.getTracks().forEach(track=>track.stop());
});
</script>
</body>
</html>

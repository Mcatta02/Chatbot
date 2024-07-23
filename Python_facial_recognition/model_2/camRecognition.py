import cv2 as cv;
webcam=cv.VideoCapture(0)
stop=False
while stop==False:
    check,frame=webcam.read()
    gray=cv.cvtColor(frame,cv.COLOR_BGR2GRAY)
    haar_cascade=cv.CascadeClassifier("Python_facial_recognition/model_2/venv/haar_face.xml")
    people = ['Marco', 'Perico']
    face_recognizer = cv.face.LBPHFaceRecognizer_create()
    face_recognizer.read('face_trained.yml')
    faces_rect=haar_cascade.detectMultiScale(gray,scaleFactor=1.1,minNeighbors=10)
    face_detected=False
    for(x,y,w,h) in faces_rect:
        faces_roi = gray[y:y + h, x:x + w]
        label, confidence = face_recognizer.predict(faces_roi)
        print(f'Label = {people[label]} with a confidence of {confidence}')
        cv.rectangle(frame,(x,y),(x+w,y+h),(0,255,0),thickness=2)
        face_detected=True
    cv.imshow('detected faces',frame)
    key = cv.waitKey(1)
    if key==ord('q'):
        print(f'Face detected')
        stop=True
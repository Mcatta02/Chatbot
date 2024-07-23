import cv2 as cv
webcam=cv.VideoCapture(0)
stop=False
while stop==False:
    check,frame=webcam.read()
    gray=cv.cvtColor(frame,cv.COLOR_BGR2GRAY)
    haar_cascade=cv.CascadeClassifier("Python_facial_recognition/model_2/venv/haar_face.xml")
    faces_rect=haar_cascade.detectMultiScale(gray,scaleFactor=1.1,minNeighbors=10)
    face_detected=False
    for(x,y,w,h) in faces_rect:
        cv.rectangle(frame,(x,y),(x+w,y+h),(0,255,0),thickness=2)
        face_detected=True
    cv.imshow('detected faces',frame)
    if cv.waitKey(1000) & face_detected:
        cv.waitKey(1000)
        # time.sleep(2)
        print(f'Face detected')
        stop=True
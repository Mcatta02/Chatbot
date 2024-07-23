from nltk.tokenize import word_tokenize
from nltk.corpus import stopwords
from nltk.stem import WordNetLemmatizer
from sklearn.model_selection import train_test_split
import tensorflow as tf
from keras.models import Sequential
from keras.layers import Dense, SimpleRNN
import numpy as np
import nltk
from sklearn.feature_extraction.text import CountVectorizer
import random
import sys
nltk.data.path.append('.')

class RNN:
    vectorizer = CountVectorizer()
    #in_domain_data = []
    #out_of_domain_data = []
    def __init__(self):
        with open('output.txt', 'r') as f:
            self.in_domain_data = [line.strip() for line in f]
        with open('output1.txt', 'r') as f:
            self.out_of_domain_data = [line.strip() for line in f]
        

    def preprocess_data_first_step(self, data):
        preprocessed_data = []
        stop_words = set(stopwords.words('english'))
        lemmatizer = WordNetLemmatizer()
        
        for sentence in data:
            tokens = word_tokenize(sentence)
            new_tokens = []
            for token in tokens:
                if token.isalpha():   
                    lower_token = token.lower()
                    lemmatized_token = lemmatizer.lemmatize(lower_token)
                    new_tokens.append(lemmatized_token)
            tokens = new_tokens

            tokens_without_stop_words = []
            for token in tokens:
                if token not in stop_words:   # This line is checking if the token is not a stop word
                    tokens_without_stop_words.append(token)  # If not, it's added to the list of filtered tokens
            tokens = tokens_without_stop_words

            preprocessed_data.append(' '.join(tokens))
        
        
        X = self.vectorizer.fit_transform(preprocessed_data)
        
        return X.toarray()

    #preprocessed_in_domain_data = preprocess_data_first_step(in_domain_data)
    #print("Preprocessed In-Domain Data:")
    #print(preprocessed_in_domain_data)
    #preprocessed_out_of_domain_data = preprocess_data_first_step(out_of_domain_data)
    #print("\nPreprocessed Out-of-Domain Data:")
    #print(preprocessed_out_of_domain_data)
        
    def rnnModel(self):
        random.seed(0)
        np.random.seed(0)
        tf.random.set_seed(0)
        
        
        all_data = self.in_domain_data + self.out_of_domain_data
        labels = [1]*len(self.in_domain_data) + [0]*len(self.out_of_domain_data)

        preprocessed_data = self.preprocess_data_first_step(all_data)
        X_train, X_test, y_train, y_test = train_test_split(preprocessed_data, labels, test_size=0.3)

        X_train_rnn = X_train.reshape((X_train.shape[0], 1, X_train.shape[1]))
        X_test_rnn = X_test.reshape((X_test.shape[0], 1, X_test.shape[1]))
        
        input_dim = X_train.shape[1]
        rnn_units = 200

        model = Sequential()
        model.add(SimpleRNN(units=rnn_units, input_shape=(None, input_dim), return_sequences=True,dropout=0.2, recurrent_dropout=0.2, activation='relu'))#kernel_regularizer=regularizers.l2(0.01)
        #model.add(SimpleRNN(units=rnn_units, input_shape=(None, input_dim),dropout=0.3, recurrent_dropout=0.3, activation='relu'))#kernel_regularizer=regularizers.l2(0.01)
        # model.add(LSTM(rnn_units, return_sequences=True, dropout=0.2, recurrent_dropout=0.2))
        model.add(SimpleRNN(units=rnn_units, return_sequences=True, dropout=0.2, recurrent_dropout=0.2, activation='relu'))#kernel_regularizer=regularizers.l2(0.01)
        model.add(SimpleRNN(units=rnn_units, return_sequences=True, dropout=0.2, recurrent_dropout=0.2, activation='relu'))#kernel_regularizer=regularizers.l2(0.01)
        model.add(SimpleRNN(units=rnn_units, return_sequences=False, dropout=0.2, recurrent_dropout=0.2, activation='relu'))#kernel_regularizer=regularizers.l2(0.01)
        model.add(Dense(1, activation='sigmoid'))

        model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])

        X_train_rnn = X_train.reshape((X_train.shape[0], 1, X_train.shape[1]))
        X_test_rnn = X_test.reshape((X_test.shape[0], 1, X_test.shape[1]))

        y_train = np.array(y_train)
        y_test = np.array(y_test)

        model.fit(X_train_rnn, y_train, epochs=11, verbose=0)
        return model, X_test_rnn, y_test

    def classify_user_input(self, user_input, model, vectorizer):
        stop_words = set(stopwords.words('english'))
        lemmatizer = WordNetLemmatizer()
        tokens = word_tokenize(user_input)
        tokens = [lemmatizer.lemmatize(token.lower()) for token in tokens if token.isalpha()]
        tokens = [token for token in tokens if token not in stop_words]
        preprocessed_input = [' '.join(tokens)]
        
        X_user = vectorizer.transform(preprocessed_input)
        X_user_rnn = X_user.toarray().reshape((X_user.shape[0], 1, X_user.shape[1]))

        prediction = model.predict(X_user_rnn)[0][0]
        return "In-domain" if prediction > 0.5 else "Out-of-domain"

    def run(self, sentence):
        rnnModel1, X_test_rnn_1, y_test_1 = self.rnnModel()
        #test_loss, test_accuracy = rnnModel1.evaluate(X_test_rnn_1, y_test_1)
        #print(f'Test Loss: {test_loss}, Test Accuracy: {test_accuracy}')
        user_input = sentence
        classification = self.classify_user_input(user_input, rnnModel1, self.vectorizer)
        print(classification)
        return classification
        
#instnace = RNN()
#instnace.run("gkgkgkgkkkkk")

if __name__ == '__main__':
    message = sys.argv[1]
    process = RNN()
    process.run(message)
import requests
import urllib

url = 'http://localhost:9000/api/parse_body/'


def sanitize(text):
	return text.replace('/', '%2f')


def parse(text):
    response = requests.post(url, data={'text': text})
    return response.json()

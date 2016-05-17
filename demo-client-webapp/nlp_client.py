import requests

base_url = 'http://localhost:9000/api/parse/'

def sanitize(text):
	return text.replace('/', '%2f')


def parse(text):
	url = base_url + sanitize(text.strip())
	response = requests.get(url)
	return response.json()

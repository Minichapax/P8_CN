import pymongo
import io
import json 

conn_str = "mongodb://10.106.236.96:27017/API_NUBE"

client = pymongo.MongoClient(conn_str, serverSelectionTimeoutMS=5000)

with io.open('discos.json', "r", encoding="UTF-8") as f:
    collection = client.API_NUBE.discos
    for d in json.load(f):
        collection.insert_one(d)
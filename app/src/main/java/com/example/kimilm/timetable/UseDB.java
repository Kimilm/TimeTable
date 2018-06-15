package com.example.kimilm.timetable;

import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.bson.Document;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class UseDB {
	
	public static String IP = "45.119.146.33";
    public static int Port = 27017;
    
    //dbName
    public static String dbName = "User";

    //Connect to MongoDB
    public static MongoClient mongoClient = new MongoClient(IP, Port);
    public static MongoDatabase db = mongoClient.getDatabase(dbName);
    
    //user collection
    public static MongoCollection<Document> collection = db.getCollection("user");

	//계정 추가
	public static boolean insertUser (String id, String pwd, String name)
	{
		MongoCursor<Document> searchDoc = collection.find(new Document("_id", id)).iterator();

		//검색 결과가 있다면
		if(searchDoc.hasNext())
		{
			return false;
		}

		ArrayList<String> dbFriendId = new ArrayList<>();

		collection.insertOne(new Document("_id", id).append("pwd", pwd).append("name", name)
				.append("timetable", null).append("f_id", dbFriendId));

		//요놈의 테이블 진짜ㅠㅠㅠㅠㅠㅠㅠ
		uploadTimeTable (true, id);

		return true;
	}

	//Search User
	//아 깊은 복사 왜 안됔ㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋ
	//일단 급한대로 어레이리스트 사용
	public static void searchUser (ArrayList<Document> loginDoc, String id, String pwd)
	{
		loginDoc.clear();

        // id && pwd make query
        ArrayList<BasicDBObject> queryObj = new ArrayList<>();
        queryObj.add(new BasicDBObject("_id", id));
        queryObj.add(new BasicDBObject("pwd", pwd));
        
        BasicDBObject query = new BasicDBObject("$and", queryObj);
        
        // query to db
        MongoCursor<Document> cursor = collection.find(query).iterator();
        
        if (cursor.hasNext())
        {
			loginDoc.add(new Document(cursor.next()));
        }
		else
		{
			loginDoc.add(null);
		}
	}

	//유저의 타임테이블 갱신
	public static void uploadTimeTable (boolean isCurAcc, String id)
	{
		//계정이 없다면 디비에 올릴 필요 없음
		if (!isCurAcc)
		{
			return;
		}

		BasicDBObject dbId = new BasicDBObject("_id", id);

		BasicDBObject timeTableObj = new BasicDBObject("timetable", TimeTable.mkTableDBObject());

		BasicDBObject field = new BasicDBObject("$set", timeTableObj);

		collection.updateOne(dbId, field);
	}

	//유저의 친구 추가
	public static void insertFriend (String myId, String friendId)
	{
		BasicDBObject dbId = new BasicDBObject("_id", myId);

		BasicDBObject pushFriendId = new BasicDBObject("f_id", friendId);

		BasicDBObject field = new BasicDBObject("$push", pushFriendId);

		collection.updateOne(dbId, field);
	}

	//유저의 친구 삭제
	public static void deleteFriend (String myId, String friendId)
	{
		BasicDBObject dbId = new BasicDBObject("_id", myId);

		BasicDBObject pullFriendId = new BasicDBObject("f_id", friendId);

		BasicDBObject field = new BasicDBObject("$pull", pullFriendId);

		collection.updateOne(dbId, field);
	}

	//친구 타임테이블
	public static void searchFriendTable(ArrayList<Document> doc, ArrayList<String> id)
	{
		doc.clear();

		BasicDBObject inQuery = new BasicDBObject("$in", id);
		BasicDBObject query = new BasicDBObject("_id", inQuery);
		BasicDBObject nameField = new BasicDBObject("name", 1);
		BasicDBObject tableField = new BasicDBObject("timetable", 1);

		MongoCursor<Document> cursor = collection.find(query).projection(nameField).projection(tableField).iterator();

		if (cursor.hasNext())
		{
			doc.add(new Document(cursor.next()));
		}
		else
		{
			doc.add(null);
		}
	}

	//삭!제!		테스트 아직 못함
	public static void deleteAccount (String id)
	{
		BasicDBObject query = new BasicDBObject("_id", id);

		collection.deleteOne(query);
	}

	public static Friend parseToFriend (Document document)
	{
		return new Friend(document.getString("_id"), document.getString("pwd"),
				document.getString("name"), (ArrayList<String>)(document.get("f_id", ArrayList.class)));
	}
}

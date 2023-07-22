package com.driver;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class WhatsappRepository {

    Map<String,User> mobileUserMap = new HashMap<>(); //{mobile no,user}
    Map<String,Group> nameGroupMap = new HashMap<>(); // {group name, group}

    Map<Integer,Message> idMessageMap = new HashMap<>(); // {id , message }

    Map<String,String> adminGroupMap = new HashMap<>(); //{ group name, admin mobile }

    Map<String,List<Message>> groupMessageMap = new HashMap<>(); //{group name, messages}
    Map<String,List<User>> groupUserMap = new HashMap<>(); //{group name, users}

    Map<String, String> userGroupMap = new HashMap<>();

    Map<String,List<Message>> userMessageMap = new HashMap<>();



    private int groupNo = 1;

    private int messageNo = 1;



    public String createUser(String name, String mobile) throws Exception {


        if(mobileUserMap.containsKey(mobile)){
            throw new Exception("User already exists");
        }
        User user = new User(name,mobile);
        mobileUserMap.put(mobile,user);
        userMessageMap.put(user.getMobile(),new ArrayList<>());

        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {

        Group group = new Group();
        if(users.size()==2){
            group.setName(users.get(1).getName());
        }else{
            group.setName("Group "+ this.groupNo);
            this.groupNo++;
        }

        group.setNumberOfParticipants(users.size());
        adminGroupMap.put(group.getName(),users.get(0).getMobile());
        nameGroupMap.put(group.getName(), group);
        groupUserMap.put(group.getName(),users);
        groupMessageMap.put(group.getName(),new ArrayList<>());

        for(User user: users){
            userGroupMap.put(user.getMobile(),group.getName());
        }


        return group;

    }

    public int createMessage(String content) {

        Message message = new Message();
        message.setId(messageNo);
        message.setContent(content);

        messageNo++;
        idMessageMap.put(message.getId(),message);

        return message.getId();

    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {

//        if(!mobileUserMap.containsKey(sender.getMobile())){
//            throw new Exception("Invalid User");
//        }
        if(group==null || !nameGroupMap.containsKey(group.getName())){
            throw new Exception("Group does not exist");
        }

        if(!groupUserMap.get(group.getName()).contains(sender)){
            throw new Exception("You are not allowed to send message");
        }
        groupMessageMap.get(group.getName()).add(message);
        userMessageMap.get(sender.getMobile()).add(message);
//        userGroupMap.put(sender.getMobile(),group.getName());
        return groupMessageMap.get(group.getName()).size();

    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {

        if(!nameGroupMap.containsKey(group.getName())){
            throw new Exception("Group does not exist");
        }

        if(!adminGroupMap.get(group.getName()).equals(approver.getMobile())){
            throw new Exception("Approver does not have rights");
        }
        if(!groupUserMap.get(group.getName()).contains(user)){
            throw new Exception("User is not a participant" );
        }

        adminGroupMap.remove(group.getName());

        adminGroupMap.put(group.getName(),user.getMobile());

        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception {

        if(user==null) {
            throw new Exception("User not found");
        }


        if(!mobileUserMap.containsKey(user.getMobile())){
            throw new Exception("User not found");
        }

        if(adminGroupMap.containsKey(user.getMobile())){
            throw new Exception("Cannot remove admin");
        }
        User originalUser = mobileUserMap.get(user.getMobile());

        mobileUserMap.remove(user.getMobile());
        String groupName = userGroupMap.get(user.getMobile());


        groupUserMap.get(groupName).remove(originalUser);

        List<Message> messageList = userMessageMap.get(user.getMobile());

        for(Message message: messageList){
            groupMessageMap.get(groupName).remove(message);

        }

        userMessageMap.remove(user.getMobile());

        int overallMessages = 0;

        for(List<Message> list: groupMessageMap.values()){
            overallMessages+= list.size();
        }
        nameGroupMap.get(groupName).setNumberOfParticipants(nameGroupMap.get(groupName).getNumberOfParticipants() + 1);

        return groupUserMap.get(groupName).size() + groupMessageMap.get(groupName).size() + overallMessages;



    }
}

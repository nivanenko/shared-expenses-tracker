package splitter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import splitter.model.Gift;
import splitter.model.Group;
import splitter.model.User;
import splitter.repository.GiftRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GiftService {
    private final GiftRepository giftRepository;
    private final GroupService groupService;

    @Autowired
    public GiftService(GiftRepository giftRepository, GroupService groupService) {
        this.giftRepository = giftRepository;
        this.groupService = groupService;
    }

    public Gift createGift(User giver, User receiver) {
        Gift gift = new Gift();
        gift.setGiver(giver);
        gift.setReceiver(receiver);
        return giftRepository.save(gift);
    }

    /**
     * Creates random gift pairs within the specified group.
     *
     * @param group the group for which to create gift pairs
     * @return a list of randomly assigned gift pairs
     */
    public List<Gift> createRandomGiftPairs(Group group) {
        List<User> users = new ArrayList<>(groupService.getUsers(group));
        Collections.shuffle(users);

        List<User> givers = new ArrayList<>(users);
        List<User> receivers = new ArrayList<>(users);

        Collections.rotate(receivers, 2); // Shift the receivers by two positions

        List<Gift> gifts = new ArrayList<>();
        int size = users.size();

        for (int i = 0; i < size; i++) {
            User giver = givers.get(i);
            User receiver = receivers.get(i);
            Gift gift = createGift(giver, receiver);
            gifts.add(gift);
        }
        return gifts;
    }
}
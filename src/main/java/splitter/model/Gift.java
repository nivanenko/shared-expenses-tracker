package splitter.model;

import jakarta.persistence.*;

@Entity
@Table(name = "gifts")
public class Gift implements Comparable<Gift> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gift_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "giver_id")
    private User giver;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    public User getGiver() {
        return giver;
    }

    public void setGiver(User giver) {
        this.giver = giver;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    @Override
    public int compareTo(Gift gift) {
        return giver.getName().compareToIgnoreCase(gift.getGiver().getName());
    }

    @Override
    public String toString() {
        return giver.getName() + " gift to " + receiver.getName();
    }
}
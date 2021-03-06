package org.thoughtcrime.securesms;

import org.thoughtcrime.securesms.database.model.MessageRecord;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * A search handler that contains and implements all the necessary functionality to 
 * search through a conversation's message records and return positions of the searched messages
 */
public class SearchHandler {
    private LinkedList<MessageRecord> messageRecordList;
    private LinkedList<SearchResult>  searchResultList;
    private int                       searchIndex = -1;
    private int                       positionIndex = 0;
    private int                       counter       = 0;
    private String                    searchedTerm = null;

    public SearchHandler() {
        messageRecordList = new LinkedList<>();
        searchResultList = new LinkedList<>();
    }

    /**
     * Searches through the message records bodies for occurrence of the term and pushes
     * the message records into searchResultList if found
     * @param term
     * @return
     */
    public void search(String term) {
        counter = 0;
        positionIndex = 0;
        searchIndex = -1;
        searchResultList.clear();
        searchedTerm = term;

        /**
         *   Search messageRecordList and push position (which is the index of the list)
         *   and messageRecord into searchResultList
         */
        Iterator<MessageRecord> iterator = messageRecordList.iterator();
        while (iterator.hasNext()) {
            MessageRecord messageRecord = iterator.next();
            if (messageRecord.getBody().getBody().toLowerCase().contains(term.toLowerCase())) {
                SearchResult searchResult = new SearchResult(positionIndex, messageRecord);
                searchResultList.add(searchResult);
            }
            positionIndex++;
        }
    }

    /**
     * Adds to the front of the messageRecordList
     * @param messageRecord
     * @return
     */
    public void addMessageRecord(MessageRecord messageRecord) {
        boolean is_new = true;

        for (MessageRecord m : messageRecordList) {
            if (m.getId() == messageRecord.getId()) is_new = false;
        }

        if (is_new) messageRecordList.addFirst(messageRecord);
    }

    /**
     * Adds to the front of the searchResultList
     * @param messageRecord
     * @return
     */
    public void addSearchedResult(int position, MessageRecord messageRecord) {
        searchResultList.addFirst(new SearchResult(position, messageRecord));
    }

    /**
     * Deletes the message record from messageRecordList with the id
     * @param messageId
     * @return
     */
    public void deleteMessageRecord(long messageId) {
        Iterator<MessageRecord> iterator = messageRecordList.iterator();
        
        while (iterator.hasNext()) {
            MessageRecord messageRecord = iterator.next();
            if (messageRecord.getId() == messageId) {
                iterator.remove();
                continue;
            }
        }

        SearchResult sr = new SearchResult(-1, null);
        for(SearchResult searchResult : searchResultList) {
            if (searchResult.getMessageRecord().getId() == messageId) {
                sr = searchResult;
                break;
            }
        }

        searchResultList.remove(sr);
    }

    /**
     * Returns the next position of the searchedResultList
     * @return int representing the position
     */
    public int getNextResultPosition() {
        if (searchIndex < getResultNumber() - 1) {
            counter++;
            return searchResultList.get(++searchIndex).getPosition();
        }
        return -1;
    }

    /**
     * Returns the previous position of the searchedResultList
     * @return int representing the position
     */
    public int getPreviousResultPosition() {
        if (searchIndex > 0) {
            counter--;
            return searchResultList.get(--searchIndex).getPosition();
        }
        return -1;
    }

    /**
     * This method is used by the conversationfragment to show as search counter
     * when pressing next and previous keys
     * @return
     */
    public int getCounter() {
        return this.counter;
    }

    /**
     * Checks if the given message record is contained in the searchedResultList
     * @param searchedMessageRecord
     * @return boolean
     */
    public boolean isSearchedMessage(MessageRecord searchedMessageRecord) {
        Iterator<SearchResult> iterator = searchResultList.iterator();
        
        while (iterator.hasNext()) {
            SearchResult messageRecord = iterator.next();
            if (messageRecord.getMessageRecord().getId() == searchedMessageRecord.getId()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Resets the search handler state
     * @return 
     */
    public void resetSearchHandler() {
        searchResultList.clear();
        searchIndex = -1;
        searchedTerm = null;
    }

    public boolean hasResults() {
        return searchResultList.size() > 0;
    }

    public int getResultNumber() {
        return searchResultList.size();
    }

    public boolean hasMessageRecords() {
        return messageRecordList.size() > 0;
    }

    public String getSearchedTerm() {
        return searchedTerm;
    }

    public void setMessageRecordList(LinkedList<MessageRecord> messageRecordList) {
        this.messageRecordList = messageRecordList;
    }

    public LinkedList<MessageRecord> getMessageRecordList() {
        return messageRecordList;
    }

    public LinkedList<SearchResult> getSearchResultList() {
        return searchResultList;
    }

    public class SearchResult {
        private int position;
        private MessageRecord messageRecord;

        public SearchResult(int position, MessageRecord messageRecord) {
            this.position = position;
            this.messageRecord = messageRecord;
        }

        public int getPosition() {
            return position;
        }

        public MessageRecord getMessageRecord() {
            return messageRecord;
        }
    }
}

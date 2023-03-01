package fr.miage.filestore.files.entity;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(indexes = { @Index(name = "node_idx", columnList = "parent, name") })
@NamedQueries({
        @NamedQuery(name = "Node.listChildren", query = "SELECT n FROM Node n WHERE n.parent = :parent"),
        @NamedQuery(name = "Node.countChildren", query = "SELECT count(n) FROM Node n WHERE n.parent = :parent"),
        @NamedQuery(name = "Node.findChildrenForName", query = "SELECT n FROM Node n WHERE n.parent = :parent AND n.name = :name"),
        @NamedQuery(name = "Node.countChildrenForName", query = "SELECT count(n) FROM Node n WHERE n.parent = :parent AND n.name = :name")
})
public class Node implements Comparable<Node>, Serializable {

    public static final String ROOT_ID = "root";
    public static final String TREE_MIMETYPE = "application/fs-folder";

    @Enumerated(EnumType.STRING)
    private Type type;
    @Id
    private String id;
    @Version
    private long version;
    private String parent;
    private String name;
    @Column(length = 50)
    private String mimetype;
    private long size;
    private long creation;
    private long modification;
    @JsonbTransient
    private String contentId;
    @Transient
    private float searchResultScore;
    @Transient
    private String searchResultExplanation;

    public Node() {
        this.creation = this.modification = System.currentTimeMillis();
        this.size = 0;
    }

    public Node(Type type, String parent, String id, String name) {
        this();
        this.type = type;
        this.id = id;
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String content) {
        this.contentId = content;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getCreation() {
        return creation;
    }

    public void setCreation(long creation) {
        this.creation = creation;
    }

    public long getModification() {
        return modification;
    }

    public void setModification(long modification) {
        this.modification = modification;
    }

    public boolean isRoot() {
        return this.id.equals(ROOT_ID);
    }

    public boolean isFolder() {
        return this.type.equals(Type.TREE);
    }

    public float getSearchResultScore() {
        return searchResultScore;
    }

    public void setSearchResultScore(float searchResultScore) {
        this.searchResultScore = searchResultScore;
    }

    public String getSearchResultExplanation() {
        return searchResultExplanation;
    }

    public void setSearchResultExplanation(String searchResultExplanation) {
        this.searchResultExplanation = searchResultExplanation;
    }

    public enum Type {
        TREE,
        BLOB
    }

    @Override
    public int compareTo(Node o) {
        if (this.getType().equals(o.getType())) {
            return this.getName().compareTo(o.getName());
        } else if (this.getType().equals(Type.TREE)){
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return creation == node.creation && modification == node.modification && type == node.type && Objects.equals(id, node.id) && Objects.equals(name, node.name) && Objects.equals(contentId, node.contentId) && Objects.equals(mimetype, node.mimetype);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id, name, contentId, mimetype, creation, modification);
    }

    @Override
    public String toString() {
        return "Node{" +
                "type=" + type +
                ", id='" + id + '\'' +
                ", version=" + version +
                ", parent='" + parent + '\'' +
                ", name='" + name + '\'' +
                ", mimetype='" + mimetype + '\'' +
                ", size=" + size +
                ", creation=" + creation +
                ", modification=" + modification +
                ", contentId='" + contentId + '\'' +
                ", searchResultScore=" + searchResultScore +
                ", searchResultExplanation='" + searchResultExplanation + '\'' +
                '}';
    }

    public static class NameComparatorAsc implements Comparator<Node> {
        @Override
        public int compare(Node o1, Node o2) {
            if ( o1.isFolder() && !o2.isFolder() ) {
                return -1;
            }
            if ( !o1.isFolder() && o2.isFolder() ) {
                return 1;
            }
            return o1.getName().compareTo(o2.getName());
        }
    }

    public static class NameComparatorDesc implements Comparator<Node> {
        @Override
        public int compare(Node o1, Node o2) {
            if ( o1.isFolder() && !o2.isFolder() ) {
                return -1;
            }
            if ( !o1.isFolder() && o2.isFolder() ) {
                return 1;
            }
            return o2.getName().compareTo(o1.getName());
        }
    }
}
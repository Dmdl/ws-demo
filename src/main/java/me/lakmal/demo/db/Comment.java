package me.lakmal.demo.db;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@AllArgsConstructor
@NoArgsConstructor
@Table("private_comment")
public class Comment {
    @Id
    private int _id;
    private String id;
    private String parent;
    private String object;
    private String author;
    private String content;
}

package fr.epita.assistants.data.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "course_model")
public class CourseModel {
    public String name;
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public @ElementCollection @CollectionTable(name = "course_model_tags", joinColumns = @JoinColumn(name = "course_id")) List<String> tag;
}

package fr.epita.assistants.data.model;

import javax.persistence.*;

@Entity
@Table(name = "student_model")
public class StudentModel {
    public String name;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) @JoinColumn
    public Long id;

    @ManyToOne(targetEntity = CourseModel.class) @JoinColumn(name = "course_id") public Long course_id;
}

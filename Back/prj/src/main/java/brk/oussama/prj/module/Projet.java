package brk.oussama.prj.module;

import jakarta.persistence.*;

@Entity
public class Projet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String path;
    @Lob
    private String javafile;
    @Lob
    private String entity;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Projet(Long id, String nom, String path, String javafile, String entity, User user) {
        this.id = id;
        this.nom = nom;
        this.path = path;
        this.javafile = javafile;
        this.entity = entity;
        this.user = user;
    }

    public Projet() {
    }

    public static ProjetBuilder builder() {
        return new ProjetBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public String getNom() {
        return this.nom;
    }

    public String getPath() {
        return this.path;
    }

    public String getJavafile() {
        return this.javafile;
    }

    public String getEntity() {
        return this.entity;
    }

    public User getUser() {
        return this.user;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setJavafile(String javafile) {
        this.javafile = javafile;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Projet)) return false;
        final Projet other = (Projet) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$nom = this.getNom();
        final Object other$nom = other.getNom();
        if (this$nom == null ? other$nom != null : !this$nom.equals(other$nom)) return false;
        final Object this$path = this.getPath();
        final Object other$path = other.getPath();
        if (this$path == null ? other$path != null : !this$path.equals(other$path)) return false;
        final Object this$javafile = this.getJavafile();
        final Object other$javafile = other.getJavafile();
        if (this$javafile == null ? other$javafile != null : !this$javafile.equals(other$javafile)) return false;
        final Object this$entity = this.getEntity();
        final Object other$entity = other.getEntity();
        if (this$entity == null ? other$entity != null : !this$entity.equals(other$entity)) return false;
        final Object this$user = this.getUser();
        final Object other$user = other.getUser();
        if (this$user == null ? other$user != null : !this$user.equals(other$user)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Projet;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $nom = this.getNom();
        result = result * PRIME + ($nom == null ? 43 : $nom.hashCode());
        final Object $path = this.getPath();
        result = result * PRIME + ($path == null ? 43 : $path.hashCode());
        final Object $javafile = this.getJavafile();
        result = result * PRIME + ($javafile == null ? 43 : $javafile.hashCode());
        final Object $entity = this.getEntity();
        result = result * PRIME + ($entity == null ? 43 : $entity.hashCode());
        final Object $user = this.getUser();
        result = result * PRIME + ($user == null ? 43 : $user.hashCode());
        return result;
    }

    public String toString() {
        return "Projet(id=" + this.getId() + ", nom=" + this.getNom() + ", path=" + this.getPath() + ", javafile=" + this.getJavafile() + ", entity=" + this.getEntity() + ", user=" + this.getUser() + ")";
    }

    public static class ProjetBuilder {
        private Long id;
        private String nom;
        private String path;
        private String javafile;
        private String entity;
        private User user;

        ProjetBuilder() {
        }

        public ProjetBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ProjetBuilder nom(String nom) {
            this.nom = nom;
            return this;
        }

        public ProjetBuilder path(String path) {
            this.path = path;
            return this;
        }

        public ProjetBuilder javafile(String javafile) {
            this.javafile = javafile;
            return this;
        }

        public ProjetBuilder entity(String entity) {
            this.entity = entity;
            return this;
        }

        public ProjetBuilder user(User user) {
            this.user = user;
            return this;
        }

        public Projet build() {
            return new Projet(this.id, this.nom, this.path, this.javafile, this.entity, this.user);
        }

        public String toString() {
            return "Projet.ProjetBuilder(id=" + this.id + ", nom=" + this.nom + ", path=" + this.path + ", javafile=" + this.javafile + ", entity=" + this.entity + ", user=" + this.user + ")";
        }
    }
}

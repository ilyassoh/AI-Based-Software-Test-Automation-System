package brk.oussama.prj.dto;

public class Codedto {
    public static class GitRequest {
        public String url;

        public GitRequest(String url) {
            this.url = url;
        }

        public GitRequest() {
        }

        public static GitRequestBuilder builder() {
            return new GitRequestBuilder();
        }

        public String getUrl() {
            return this.url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof GitRequest)) return false;
            final GitRequest other = (GitRequest) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$url = this.getUrl();
            final Object other$url = other.getUrl();
            if (this$url == null ? other$url != null : !this$url.equals(other$url)) return false;
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof GitRequest;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $url = this.getUrl();
            result = result * PRIME + ($url == null ? 43 : $url.hashCode());
            return result;
        }

        public String toString() {
            return "Codedto.GitRequest(url=" + this.getUrl() + ")";
        }

        public static class GitRequestBuilder {
            private String url;

            GitRequestBuilder() {
            }

            public GitRequestBuilder url(String url) {
                this.url = url;
                return this;
            }

            public GitRequest build() {
                return new GitRequest(this.url);
            }

            public String toString() {
                return "Codedto.GitRequest.GitRequestBuilder(url=" + this.url + ")";
            }
        }
    }

    public static class Path {
        public String path;

        public Path(String path) {
            this.path = path;
        }

        public Path() {
        }

        public static PathBuilder builder() {
            return new PathBuilder();
        }

        public String getPath() {
            return this.path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof Path)) return false;
            final Path other = (Path) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$path = this.getPath();
            final Object other$path = other.getPath();
            if (this$path == null ? other$path != null : !this$path.equals(other$path)) return false;
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof Path;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $path = this.getPath();
            result = result * PRIME + ($path == null ? 43 : $path.hashCode());
            return result;
        }

        public String toString() {
            return "Codedto.Path(path=" + this.getPath() + ")";
        }

        public static class PathBuilder {
            private String path;

            PathBuilder() {
            }

            public PathBuilder path(String path) {
                this.path = path;
                return this;
            }

            public Path build() {
                return new Path(this.path);
            }

            public String toString() {
                return "Codedto.Path.PathBuilder(path=" + this.path + ")";
            }
        }
    }
}

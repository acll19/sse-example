package org.aca.resources.response;

import javax.json.Json;

public record Progress(String status, int progress) {
    @Override
    public String toString() {
        return Json.createObjectBuilder()
                .add("status", this.status)
                .add("progress", this.progress)
                .build()
                .toString();
    }
}

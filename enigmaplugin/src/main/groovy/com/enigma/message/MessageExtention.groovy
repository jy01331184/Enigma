package com.enigma.message;

import org.gradle.api.Project;

/**
 * Created by tianyang on 17/5/4.
 */
public class MessageExtention {

    private boolean staticLink;

    private boolean genEvent;

    private Project project;

    public MessageExtention(Project project) {
        this.project = project;
    }

    @Override
    public String toString() {
        return "MessageExtention{" +
                "staticLink=" + staticLink +
                ", genEvent=" + genEvent +
                '}';
    }

    public boolean isStaticLink() {
        return staticLink;
    }

    public void setStaticLink(boolean staticLink) {
        this.staticLink = staticLink;
        apt()
    }

    public boolean isGenEvent() {
        return genEvent;
    }

    public void setGenEvent(boolean genEvent) {
        this.genEvent = genEvent;
        apt()
    }

    private void apt(){
        if(genEvent && staticLink){
            //println("[MessageCenterPlugin cannot genEvent & staticLink. turn off staticLink]")
            //staticLink = false;
        }

        project.apt  {
            arguments {
                slink Boolean.toString(staticLink)
                event Boolean.toString(genEvent)
            }
        }
    }
}

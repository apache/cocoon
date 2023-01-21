/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.forms.samples.dreamteam;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Manager
 */
public class Manager {
    private static Manager manager = new Manager();
    private List allPersons = null;
    private Team dreamTeam = null;

    private Manager() {
        super();
    }

    public static Manager getManager() {
        if (manager == null) {
            manager = new Manager();
        }
        return manager;
    }
    public Hashtable getAllPersonsByPosition() {
        Hashtable list = new Hashtable();
        Team team = null;
        TeamMember teamMember = null;
        String position = null;
        for (int i = 0; i < allPersons.size(); i++) {
            teamMember = (TeamMember)allPersons.get(i);
            position = teamMember.getPosition();
            if (list.containsKey(position)) {
                team = (Team)list.get(position);
            }
            else {
                team = new Team();
                list.put(position, team);
            }
            team.addMember(teamMember);
        }
        return list;

    }
    public TeamMember getPerson(String memberID) {
        TeamMember member = null;
        if (allPersons == null) {
            return null;
        }
        for (Iterator iter = allPersons.iterator(); iter.hasNext();) {
            member = (TeamMember) iter.next();
            if (member.getMemberId().equals(memberID)) {
                return member;
            }
        }
        return null;
    }

    public void buildDreamTeam(Team dreamlist) {
        if (dreamTeam != null) {
            dreamTeam.getTeam().clear();
        }
        dreamTeam = new Team();
        TeamMember listMember = null;
        TeamMember dreamMember = null;
        for (Iterator iter = dreamlist.getTeam().iterator(); iter.hasNext();) {
            listMember = (TeamMember) iter.next();
            dreamMember = getPerson(listMember.getMemberId());
            dreamTeam.addMember(dreamMember);
        }
    }

    public Team getDreamTeam() {
        if (dreamTeam == null) {
            dreamTeam = new Team();
            dreamTeam.setTeam(allPersons);
        }
        return dreamTeam;
    }

    public void readPlayers(Document doc) {
        NodeList players = doc.getElementsByTagName("player");
        Node player = null;
        int size = players.getLength();
        for (int i = 0; i < size; i++) {
            player = players.item(i);
            addPlayer((Element)player);
        }
    }

    private void addPlayer(Element player) {
        // convert the XML node to a TeamMember
        if (allPersons == null) {
            allPersons = new ArrayList();
        }
        TeamMember member = null;
        member = new TeamMember();
        member.setMemberId("" + (allPersons.size() + 1));
        member.setName(getElementValue(player, "name"));
        member.setPosition(getElementValue(player, "position"));
        member.setCountry(getElementValue(player, "country"));
//        System.out.println(member.toString());
        allPersons.add(member);
    }

    private String getElementValue(Element element, String tag) {
        String result = null;
        NodeList nodes = element.getElementsByTagName(tag);
        result = nodes.item(0).getFirstChild().getNodeValue();
//        System.out.println("element: " + tag + "=" + result);
        return result;
    }

}

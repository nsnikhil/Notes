package com.nrs.nsnik.notes

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * File-based test that reads nav_graph.xml and verifies
 * all expected destinations and actions are declared.
 */
class NavigationGraphTest {

    private lateinit var fragmentIds: List<String>
    private lateinit var actionIds: List<String>

    @Before
    fun parseNavGraph() {
        val navGraphFile = File("src/main/res/navigation/nav_graph.xml")
        assertTrue("nav_graph.xml must exist", navGraphFile.exists())

        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(navGraphFile)

        val fragments = document.getElementsByTagName("fragment")
        val fragmentIdList = mutableListOf<String>()
        val actionIdList = mutableListOf<String>()

        for (i in 0 until fragments.length) {
            val node = fragments.item(i)
            val id = node.attributes.getNamedItem("android:id")?.nodeValue
            if (id != null) fragmentIdList.add(id)

            val children = node.childNodes
            for (j in 0 until children.length) {
                val child = children.item(j)
                if (child.nodeName == "action") {
                    val actionId = child.attributes.getNamedItem("android:id")?.nodeValue
                    if (actionId != null) actionIdList.add(actionId)
                }
            }
        }

        fragmentIds = fragmentIdList
        actionIds = actionIdList
    }

    @Test
    fun navGraph_containsExactlySevenDestinations() {
        assertEquals("Expected 7 fragment destinations", 7, fragmentIds.size)
    }

    @Test
    fun navGraph_containsListFragment() {
        assertTrue(fragmentIds.any { it.contains("navItemNotes") })
    }

    @Test
    fun navGraph_containsNewNoteFragment() {
        assertTrue(fragmentIds.any { it.contains("newNoteFragment") })
    }

    @Test
    fun navGraph_containsSearchFragment() {
        assertTrue(fragmentIds.any { it.contains("searchFragment") })
    }

    @Test
    fun navGraph_containsAboutFragment() {
        assertTrue(fragmentIds.any { it.contains("navItemAbout") })
    }

    @Test
    fun navGraph_containsPrefFragment() {
        assertTrue(fragmentIds.any { it.contains("navItemSettings") })
    }

    @Test
    fun navGraph_containsIntroFragment() {
        assertTrue(fragmentIds.any { it.contains("introFragment") })
    }

    @Test
    fun navGraph_containsImageListFragment() {
        assertTrue(fragmentIds.any { it.contains("imageListFragment") })
    }

    @Test
    fun navGraph_containsListToAddNewNoteAction() {
        assertTrue(actionIds.any { it.contains("listToAddNewNote") })
    }

    @Test
    fun navGraph_containsListToSearchAction() {
        assertTrue(actionIds.any { it.contains("listToSearch") })
    }

    @Test
    fun navGraph_containsNoteToImageListAction() {
        assertTrue(actionIds.any { it.contains("noteToImageList") })
    }

    @Test
    fun navGraph_containsIntroToNoteListAction() {
        assertTrue(actionIds.any { it.contains("introToNoteList") })
    }

    @Test
    fun navGraph_hasFourActions() {
        assertEquals("Expected 4 actions in nav graph", 4, actionIds.size)
    }
}

package de.contagio.core.usecase

import de.contagio.core.domain.entity.Address
import de.contagio.core.domain.entity.Person
import de.contagio.core.domain.entity.Tester
import de.contagio.core.domain.entity.Teststation
import de.contagio.core.domain.port.IFindAllTester
import de.contagio.core.domain.port.IFindAllTeststation
import kotlin.test.Test
import kotlin.test.assertEquals

class ListOfAllTesterInfoTest {

    @Test
    fun emptyTeststationEmptyTester_isEmpty() {
        val findAllTeststation = IFindAllTeststation {
            emptyList()
        }

        val findAllTester = IFindAllTester {
            emptyList()
        }

        val listOfAllTester = ListOfAllTesterInfo(findAllTeststation, findAllTester).execute()

        assertEquals(0, listOfAllTester.size)
    }

    @Test
    fun emptyTeststationWithOneTester_isEmpty() {
        val findAllTeststation = IFindAllTeststation {
            emptyList()
        }

        val findAllTester = IFindAllTester {
            listOf(
                Tester(
                    id = "1",
                    teststationId = "1",
                    person = Person(firstName = "hugo", lastName = "Blafasel")
                )
            )
        }

        val listOfAllTester = ListOfAllTesterInfo(findAllTeststation, findAllTester).execute()

        assertEquals(0, listOfAllTester.size)
    }

    @Test
    fun oneTeststationWithNotMatchingTester_isEmpty() {
        val findAllTeststation = IFindAllTeststation {
            listOf(
                Teststation(
                    id = "0",
                    name = "teststation",
                    address = Address("blacity", "1234", "blastreet")
                )
            )
        }

        val findAllTester = IFindAllTester {
            listOf(
                Tester(
                    id = "1",
                    teststationId = "1",
                    person = Person(firstName = "hugo", lastName = "Blafasel")
                )
            )
        }

        val listOfAllTester = ListOfAllTesterInfo(findAllTeststation, findAllTester).execute()

        assertEquals(0, listOfAllTester.size)
    }

    @Test
    fun oneTeststationWithMatchingTester_expectedDisplayname() {
        val findAllTeststation = IFindAllTeststation {
            listOf(
                Teststation(
                    id = "1",
                    name = "teststation",
                    address = Address("blacity", "1234", "blastreet")
                )
            )
        }

        val findAllTester = IFindAllTester {
            listOf(
                Tester(
                    id = "1",
                    teststationId = "1",
                    person = Person(firstName = "hugo", lastName = "Blafasel")
                )
            )
        }

        val listOfAllTester = ListOfAllTesterInfo(findAllTeststation, findAllTester).execute()

        assertEquals(1, listOfAllTester.size)
        assertEquals("hugo Blafasel in teststation", listOfAllTester["1"]?.displayInfo)
    }
}
